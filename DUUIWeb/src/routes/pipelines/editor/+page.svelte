<script lang="ts">
	import { goto, invalidateAll } from '$app/navigation'
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
		faCopy,
		faPlus,
		faSearch
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
	import { Api, makeApiCall } from '$lib/utils/api'
	import { info, success, variantError, variantSuccess } from '$lib/utils/ui'
	import ActionButton from '$lib/svelte/widgets/action/ActionButton.svelte'
	import PipelineComponent from '$lib/svelte/widgets/duui/PipelineComponent.svelte'
	import { currentPipelineStore } from '$lib/store'
	import SettingsMapper from '$lib/svelte/widgets/input/Mapper.svelte'
	import { includes } from '$lib/utils/text'
	import TextArea from '$lib/svelte/widgets/input/TextArea.svelte'
	import ComponentTemplates from './ComponentTemplates.svelte'
	import Text from '$lib/svelte/widgets/input/Text.svelte'
	import Search from '$lib/svelte/widgets/input/Search.svelte'
	import { page } from '$app/stores'

	export let data
	let { templateComponents, templatePipelines } = data
	const isImport: boolean = ($page.url.searchParams.get('import') || '') === 'true'

	templateComponents = templateComponents.map((c) => {
		return { ...c, id: uuidv4() }
	})

	let step: number = 0

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
		const response = await makeApiCall(Api.Pipelines, 'POST', $currentPipelineStore)
		if (response.ok) {
			const data = await response.json()
			toastStore.trigger(success('Pipeline created successfully'))
			goto(`/pipelines/${data.oid}`)
		}
	}

	let flipDurationMs = 300

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

	const selectPipelineTemplate = (template: DUUIPipeline) => {
		$currentPipelineStore = cloneDeep(template)

		$currentPipelineStore.oid = uuidv4()
		$currentPipelineStore.components = $currentPipelineStore.components.map((c) => {
			return { ...c, id: uuidv4(), index: $currentPipelineStore.components.indexOf(c) }
		})

		goto('/pipelines/editor?step=2')

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

	// const copyUserPipeline = async (pipeline: DUUIPipeline) => {
	// 	$currentPipelineStore = cloneDeep(pipeline)
	// 	$currentPipelineStore.oid = uuidv4()
	// 	$currentPipelineStore.components = $currentPipelineStore.components.map((c) => {
	// 		return { ...c, id: uuidv4(), index: $currentPipelineStore.components.indexOf(c) }
	// 	})

	// 	goto('/pipelines/editor?step=2')
	// 	step = 1
	// }

	$: {
		if (settings) {
			$currentPipelineStore.settings = settings
		}
	}
</script>

<div class="container h-full flex-col mx-auto flex gap-4 md:my-8">
	<h1 class="h2">
		{step === 0 ? 'Choose a starting point' : $currentPipelineStore.name || 'New Pipeline'}
	</h1>

	<hr class="bg-surface-400/20 h-[1px] !border-0 rounded" />
	<div class="flex items-center justify-between gap-4">
		{#if step === 0}
			<ActionButton
				text="Cancel"
				variant={variantError}
				icon={faCancel}
				on:click={() => goto('/pipelines')}
			/>
		{:else}
			<ActionButton
				text="Back"
				variant={variantError}
				icon={faArrowLeft}
				on:click={() => (step -= 1)}
			/>
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
			<ActionButton
				text="Create"
				icon={faCheck}
				disabled={$currentPipelineStore.components.length === 0}
				variant={variantSuccess}
				on:click={createPipeline}
				_class={$currentPipelineStore.name === '' ? 'invisible' : 'visible'}
			/>
		{/if}
	</div>

	{#if step === 0}
		<div class="space-y-4">
			<div class="grid grid-cols-2 gap-4">
				<button
					class="pipeline-card grid grid-rows-3 items-start text-left shadow-lg p-4 hover:variant-glass bg-surface-100 dark:variant-soft-surface dark:hover:bg-surface-800 space-y-4"
					on:click={() => selectPipelineTemplate(blankPipeline())}
				>
					<div class="flex items-center gap-4 justify-between">
						<p class="text-lg font-bold">Start from scratch</p>
						<p class="variant-glass-tertiary badge">New</p>
					</div>

					<p class="row-span-2">An empty Pipeline.</p>
					<div class="flex items-center justify-end gap-4">
						{#each DUUIDrivers as driver}
							<DriverIcon {driver} />
						{/each}
					</div>
				</button>
				<!-- TODO -->
				<!-- on:click={() => copyUserPipeline(blankPipeline())}  -->
				<!-- 
				<button
					class="pipeline-card grid grid-rows-3 items-start text-left shadow-lg p-4 hover:variant-glass bg-surface-100 dark:variant-soft-surface dark:hover:bg-surface-800 space-y-4"
				>
					<div class="flex items-center gap-4 justify-between">
						<p class="text-lg font-bold">Start from a Copy</p>
					</div>

					<p class="row-span-2 max-w-[50ch]">
						Copy an existing Pipeline of yours and start making changes from there.
					</p>
					<div class="flex items-center justify-end gap-4 text-primary-500">
						<Fa icon={faCopy} size="2x" />
					</div>
				</button> -->
			</div>

			<div class="md:mt-16 items-start justify-start rounded-none container">
				<div class="md:flex justify-between items-end py-4 space-y-4">
					<h3 class="h3">Templates</h3>
					<Search
						style="row-start-2 col-span-2"
						bind:query={searchText}
						icon={faSearch}
						placeholder="Search..."
					/>
				</div>
				<hr class="bg-surface-400/20 h-[1px] !border-0 rounded" />
				<div class="grid md:grid-cols-2 xl:grid-cols-3 gap-4 py-4">
					{#each filteredTemplatePipelines as pipeline}
						<button
							class="pipeline-card text-left hover:variant-glass shadow-lg dark:variant-soft-surface p-4 bg-surface-100 dark:hover:bg-surface-800 space-y-4 flex flex-col"
							on:click={() => selectPipelineTemplate(pipeline)}
						>
							<p class="text-lg font-bold">{pipeline.name}</p>
							<p class="grow min-h-[50px]">{pipeline.description}</p>
							<div class="flex items-center gap-4 justify-between">
								<p class="grow">{pipeline.components.length} Component(s)</p>
								<div class="flex items-center justify-between gap-4">
									{#each usedDrivers(pipeline) as driver}
										<DriverIcon {driver} />
									{/each}
								</div>
							</div>
						</button>
					{/each}
				</div>
			</div>
		</div>
	{:else if step === 1}
		<div class="space-y-4">
			<!-- <div class="bg-surface-100 dark:variant-soft-surface p-4 shadow-lg grid md:grid-cols-3 gap-4">
				<Checkbox
					label="Start service when finished"
					name="start-service"
					bind:checked={startService}
				/>
			</div> -->
			<div class="grid md:grid-cols-2 gap-4">
				<div
					class="flex flex-col gap-4 bg-surface-100 dark:variant-soft-surface shadow-lg p-4 {$currentPipelineStore.name !==
					''
						? ''
						: 'border-[1px] border-error-400'}"
				>
					<Text label="Name" name="pipeline-name" bind:value={$currentPipelineStore.name} />

					<TextArea
						bind:value={$currentPipelineStore.description}
						label="Description"
						name="pipeline-description"
					/>
				</div>
				<div class="bg-surface-100 dark:variant-soft-surface p-4 shadow-lg">
					<SettingsMapper bind:map={settings} />
				</div>
			</div>
		</div>
	{:else if step === 2}
		<div class="space-y-4">
			<div
				bind:this={componentContainer}
				class="container space-y-8 bg-surface-100 dark:variant-soft-surface mx-auto shadow-lg p-4 md:p-16"
			>
				{#if $currentPipelineStore.components.length === 0}
					<p class="text-center h4">Add a Component to get Started</p>
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
				<Fa class="mx-auto text-surface-400" icon={faArrowDown} size="3x" />
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
