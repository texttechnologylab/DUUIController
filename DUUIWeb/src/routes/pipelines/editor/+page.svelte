<script lang="ts">
	import { goto } from '$app/navigation'
	import DriverIcon from '$lib/components/DriverIcon.svelte'
	import { cloneDeep } from 'lodash'
	import { v4 as uuidv4 } from 'uuid'
	import ComponentModal from '$lib/svelte/widgets/modal/Component.svelte'
	import {
		faArrowDown,
		faArrowLeft,
		faArrowRight,
		faCancel,
		faCheck,
		faPlus,
		faSearch,
		faTrash,
		faUpload
	} from '@fortawesome/free-solid-svg-icons'
	import { getModalStore, getToastStore } from '@skeletonlabs/skeleton'
	import { dndzone, type DndEvent } from 'svelte-dnd-action'
	import Fa from 'svelte-fa'
	import { flip } from 'svelte/animate'
	import type { ModalComponent, ModalSettings } from '@skeletonlabs/skeleton'
	import {
		blankPipeline,
		usedDrivers,
		type DUUIPipeline,
		getPipelineCategories
	} from '$lib/duui/pipeline'
	import { blankComponent, DUUIDrivers, type DUUIComponent } from '$lib/duui/component'
	import { Api, makeApiCall } from '$lib/duui/utils/api'
	import { info, success, variantError, variantSuccess } from '$lib/duui/utils/ui'
	import ActionButton from '$lib/svelte/widgets/action/ActionButton.svelte'
	import PipelineComponent from '$lib/svelte/widgets/duui/PipelineComponent.svelte'
	import { currentPipelineStore } from '$lib/store'
	import SettingsMapper from '$lib/svelte/widgets/input/Mapper.svelte'
	import { includes } from '$lib/duui/utils/text'
	import TextArea from '$lib/svelte/widgets/input/TextArea.svelte'
	import ComponentTemplates from './ComponentTemplates.svelte'
	import Text from '$lib/svelte/widgets/input/TextInput.svelte'
	import Search from '$lib/svelte/widgets/input/Search.svelte'
	import { page } from '$app/stores'
	import IconButton from '$lib/svelte/widgets/action/IconButton.svelte'
	import SpeedDial from '$lib/svelte/widgets/navigation/SpeedDial.svelte'
	import JsonPreview from '$lib/svelte/widgets/input/JsonPreview.svelte'
	import Anchor from '$lib/svelte/widgets/action/Anchor.svelte'
	import PipelineCard from '$lib/svelte/widgets/duui/PipelineCard.svelte'
	import Chips from '$lib/svelte/widgets/input/Chips.svelte'

	export let data

	let { templateComponents, templatePipelines, user } = data

	const isImport: boolean = ($page.url.searchParams.get('import') || '') === 'true'

	templateComponents = templateComponents.map((c) => {
		return { ...c, id: uuidv4() }
	})

	let step: number = +($page.url.searchParams.get('step') || '0')

	if (!isImport) {
		$currentPipelineStore = blankPipeline()
	} else {
		step = 1

		$currentPipelineStore.components.forEach((component) => {
			component.oid = uuidv4()
			component.id = uuidv4()
			component.pipelineId = $currentPipelineStore.oid
			component.status = ''
		})
	}

	let settings: Map<string, string>

	const steps: string[] = ['Start', 'Pipeline', 'Components']

	let searchText: string = ''
	let filteredTemplatePipelines: DUUIPipeline[] = templatePipelines

	$: {
		filteredTemplatePipelines = templatePipelines.filter(
			(pipeline: DUUIPipeline) =>
				includes(
					`${pipeline.name} ${pipeline.description} ${getPipelineCategories(pipeline)}`,
					searchText
				) || !searchText
		)
	}

	const toastStore = getToastStore()

	const handleDndConsider = (event: CustomEvent<DndEvent<DUUIComponent>>) => {
		$currentPipelineStore.components = event.detail.items
		$currentPipelineStore.components = [...$currentPipelineStore.components]
	}

	const handleDndFinalize = (event: CustomEvent<DndEvent<DUUIComponent>>) => {
		$currentPipelineStore.components = event.detail.items
		$currentPipelineStore.components = [...$currentPipelineStore.components]
		$currentPipelineStore.components = $currentPipelineStore.components.map((c) => {
			return { ...c, index: $currentPipelineStore.components.indexOf(c) }
		})
	}

	const createPipeline = async () => {
		$currentPipelineStore.settings = Object.fromEntries(settings.entries())
		const response = await makeApiCall(Api.Pipelines, 'POST', { pipeline: $currentPipelineStore })
		if (response.ok) {
			const data = await response.json()
			toastStore.trigger(success('Pipeline created successfully'))
			goto(`/pipelines/${data.oid}`)
		}
	}

	let flipDurationMs = 300
	let selectedTemplate: DUUIPipeline | null

	const modalStore = getModalStore()
	let componentContainer: HTMLDivElement

	const addTemplate = (template: DUUIComponent) => {
		const copyTemplate = cloneDeep(template)
		copyTemplate.id = uuidv4()

		$currentPipelineStore.components = [...$currentPipelineStore.components, copyTemplate]

		if (componentContainer) {
			componentContainer.scrollIntoView()
		}
	}

	const selectPipelineTemplate = (template: DUUIPipeline | null = null) => {
		if (template === null) {
			template = blankPipeline()
			selectedTemplate = null
		} else {
			selectedTemplate = template
		}

		$currentPipelineStore = cloneDeep(template)
		$currentPipelineStore.oid = uuidv4()
		$currentPipelineStore.components = $currentPipelineStore.components.map((c) => {
			return { ...c, id: uuidv4(), index: $currentPipelineStore.components.indexOf(c) }
		})

		goto('/pipelines/editor?step=1')

		step = 1
	}

	const componentModal: ModalComponent = {
		ref: ComponentModal
	}

	const addComponent = () => {
		let c = blankComponent($currentPipelineStore.oid, $currentPipelineStore.components.length + 1)
		new Promise<boolean>((resolve) => {
			const modal: ModalSettings = {
				type: 'component',
				component: componentModal,
				meta: { component: c, new: true },
				response: (r: boolean) => {
					resolve(r)
				}
			}
			modalStore.trigger(modal)
		})
			.then(async (accepted: boolean) => {
				if (!accepted) return

				$currentPipelineStore.components = [...$currentPipelineStore.components, c]
			})
			.catch((e) => console.log(e))
	}

	$: {
		if (settings) {
			$currentPipelineStore.settings = settings
		}
	}

	const uploadPipeline = async () => {
		$currentPipelineStore.settings = Object.fromEntries(settings.entries())
		const response = await makeApiCall(Api.Pipelines, 'POST', {
			pipeline: $currentPipelineStore,
			template: true
		})

		if (response.ok) {
			toastStore.trigger(success('Pipeline uploaded successfully'))
			goto(`/pipelines`)
		}
	}

	const deletePipeline = async () => {
		new Promise<boolean>((resolve) => {
			const modal: ModalSettings = {
				type: 'component',
				component: 'deleteModal',
				meta: {
					title: 'Delete Template',
					body: `Are you sure you want to delete the Template ${selectedTemplate?.name}?`
				},
				response: (r: boolean) => {
					resolve(r)
				}
			}

			modalStore.trigger(modal)
		}).then(async (accepted: boolean) => {
			if (!accepted) return

			let response = await makeApiCall(Api.Pipelines, 'DELETE', { oid: selectedTemplate?.oid })
			if (response.ok) {
				goto('/pipelines')
				toastStore.trigger(info('Template deleted successfully'))
			}
		})
	}
</script>

<SpeedDial>
	<svelte:fragment slot="content">
		{#if step > 0}
			<IconButton icon={faArrowLeft} on:click={() => (step -= 1)} />
			{#if selectedTemplate && user?.role === 'admin'}
				<IconButton icon={faTrash} on:click={deletePipeline} />
			{/if}
		{:else}
			<IconButton icon={faCancel} on:click={() => goto('/pipelines')} />
		{/if}
		{#if step < 2}
			<IconButton icon={faArrowRight} on:click={() => (step += 1)} />
		{:else}
			{#if user?.role === 'admin'}
				<IconButton
					icon={faUpload}
					on:click={uploadPipeline}
					disabled={$currentPipelineStore.components.length === 0}
				/>
			{/if}
			<IconButton
				icon={faCheck}
				on:click={createPipeline}
				disabled={$currentPipelineStore.components.length === 0}
			/>
		{/if}
	</svelte:fragment>
</SpeedDial>

<div class="container h-full flex-col mx-auto flex gap-4 md:my-8">
	<h1 class="h2">
		{step === 0 ? 'Choose a starting point' : $currentPipelineStore.name || 'New Pipeline'}
	</h1>

	<hr class="bg-surface-400/20 h-[1px] !border-0 rounded-full" />
	<div class="items-center justify-between gap-4 hidden md:flex">
		{#if step === 0}
			<Anchor text="Back" icon={faArrowLeft} href="/pipelines" />
		{:else}
			<ActionButton
				text="Back"
				variant={variantError}
				icon={faArrowLeft}
				leftToRight={true}
				on:click={() => (step -= 1)}
			/>

			{#if selectedTemplate && user?.role === 'admin'}
				<ActionButton
					text="Delete"
					icon={faTrash}
					on:click={deletePipeline}
					variant="variant-filled-error dark:variant-soft-error"
				/>
			{/if}
		{/if}

		{#if step < steps.length - 1}
			<ActionButton
				text="Next"
				leftToRight={false}
				icon={faArrowRight}
				on:click={() => (step += 1)}
				_class={$currentPipelineStore.name === '' ? 'invisible' : 'visible'}
			/>
		{:else if step === steps.length - 1}
			<div class="flex items-center gap-4">
				<ActionButton
					text="Create"
					icon={faCheck}
					disabled={$currentPipelineStore.components.length === 0}
					variant={variantSuccess}
					on:click={createPipeline}
				/>
				{#if user?.role === 'admin'}
					<ActionButton
						text="Publish as Template"
						icon={faUpload}
						disabled={$currentPipelineStore.components.length === 0}
						variant={variantSuccess}
						on:click={uploadPipeline}
						_class={$currentPipelineStore.name === '' ? 'invisible' : 'visible'}
					/>
				{/if}
			</div>
		{/if}
	</div>

	{#if step === 0}
		<div class="space-y-4">
			<div class="grid md:grid-cols-3 gap-4">
				<button
					class="card-fancy text-left grid grid-rows-[auto_1fr_80px]"
					on:click={() => selectPipelineTemplate()}
				>
					<div class="flex items-center gap-4 justify-between">
						<p class="text-lg font-bold">Start from scratch</p>
						<p class="variant-glass-tertiary badge">New</p>
					</div>

					<p class="row-span-2">An empty Pipeline.</p>
					<div class="flex items-center justify-end gap-4 self-end">
						{#each DUUIDrivers as driver}
							<DriverIcon {driver} />
						{/each}
					</div>
				</button>
			</div>

			<div class="md:mt-16 items-start justify-start rounded-md container">
				<div class="md:flex justify-between items-end py-4 space-y-4">
					<h3 class="h3">Templates</h3>
					<Search
						style="row-start-2 col-span-2"
						bind:query={searchText}
						icon={faSearch}
						placeholder="Search..."
					/>
				</div>
				<hr class="bg-surface-400/20 h-[1px] !border-0 rounded-full" />
				<div class="grid md:grid-cols-2 xl:grid-cols-3 gap-4 py-4">
					{#each filteredTemplatePipelines as pipeline}
						<button
							class="card-fancy text-left grid grid-rows-[auto_1fr_80px]"
							on:click={() => selectPipelineTemplate(pipeline)}
						>
							<PipelineCard {pipeline} />
						</button>
					{/each}
				</div>
			</div>
		</div>
	{:else if step === 1}
		<div class="space-y-4">
			<div class="grid md:grid-cols-2 gap-4">
				<div
					class="section-wrapper flex flex-col gap-4 p-4 {$currentPipelineStore.name !== ''
						? ''
						: 'border-error-400'}"
				>
					<Text label="Name" name="pipeline-name" bind:value={$currentPipelineStore.name} />

					<TextArea
						bind:value={$currentPipelineStore.description}
						label="Description"
						name="pipeline-description"
					/>
					<Chips label="Tags" placeholder="Add a tag..." bind:values={$currentPipelineStore.tags} />
				</div>
				<div class="section-wrapper p-4 relative">
					<JsonPreview bind:data={settings} />
					<SettingsMapper bind:map={settings} />
				</div>
			</div>
		</div>
	{:else if step === 2}
		<div class="space-y-4">
			<div
				bind:this={componentContainer}
				class="rounded-md border border-surface-200
				dark:border-surface-500 isolate container space-y-8 bg-surface-100 dark:variant-soft-surface mx-auto shadow-lg p-4 md:p-16"
			>
				{#if $currentPipelineStore.components.length === 0}
					<p class="text-center h4 font-bold">Add a Component to get Started</p>
				{/if}
				<ul
					use:dndzone={{ items: $currentPipelineStore.components, dropTargetStyle: {} }}
					on:consider={(event) => handleDndConsider(event)}
					on:finalize={(event) => handleDndFinalize(event)}
					class="grid gap-4 max-w-5xl mx-auto"
				>
					{#each $currentPipelineStore.components as component (component.id)}
						<div animate:flip={{ duration: flipDurationMs }}>
							<PipelineComponent
								{component}
								isNew={true}
								on:remove={(event) => {
									$currentPipelineStore.components = $currentPipelineStore.components.filter(
										(c) => c.oid !== event.detail.oid
									)
									componentContainer.scrollIntoView()
								}}
							/>
						</div>
					{/each}
				</ul>
				<div class="mx-auto flex items-center gap-4 justify-center">
					<ActionButton
						text="Add"
						icon={faPlus}
						variant="variant-filled-primary dark:variant-soft-primary"
						on:click={addComponent}
					/>
				</div>
				<p class="text-center">Or choose a template</p>
				<a
					href="/pipelines/editor#templates"
					class="flex justify-center text-center transition-colors duration-300 text-surface-400 hover:text-primary-500"
				>
					<Fa icon={faArrowDown} size="3x" />
				</a>
			</div>
			<div id="templates">
				<ComponentTemplates
					components={templateComponents}
					on:select={(event) => addTemplate(event.detail.component)}
				/>
			</div>
		</div>
	{/if}
</div>
