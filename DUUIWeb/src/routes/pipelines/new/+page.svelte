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
		faCheck,
		faPlus,
		faSearch
	} from '@fortawesome/free-solid-svg-icons'
	import { getModalStore, getToastStore } from '@skeletonlabs/skeleton'
	import { dndzone, type DndEvent } from 'svelte-dnd-action'
	import Fa from 'svelte-fa'
	import { flip } from 'svelte/animate'

	import type { ModalComponent, ModalSettings } from '@skeletonlabs/skeleton'
	import TemplateModal from './TemplateModal.svelte'
	import {
		blankPipeline,
		usedDrivers,
		type DUUIPipeline,
		getPipelineCategories
	} from '$lib/duui/pipeline'
	import { blankComponent, DUUIDrivers, type DUUIComponent } from '$lib/duui/component'
	import { Api, makeApiCall } from '$lib/utils/api'
	import { success } from '$lib/utils/ui'
	import ActionButton from '$lib/svelte/widgets/action/ActionButton.svelte'
	import PipelineComponent from '$lib/components/PipelineComponent.svelte'
	import { currentPipelineStore } from '$lib/store'
	import SettingsMapper from '$lib/svelte/widgets/input/SettingsMapper.svelte'
	import { includes } from '$lib/utils/text'
	import TextInput from '$lib/svelte/widgets/input/TextInput.svelte'
	import TextArea from '$lib/svelte/widgets/input/TextArea.svelte'
	import CheckButton from '$lib/svelte/widgets/action/CheckButton.svelte'
	import ComponentTemplates from './ComponentTemplates.svelte'
	import { fly } from 'svelte/transition'
	import Text from '$lib/svelte/widgets/input/Text.svelte'
	import Password from '$lib/svelte/widgets/input/Password.svelte'
	import Checkbox from '$lib/svelte/widgets/input/Checkbox.svelte'
	import Number from '$lib/svelte/widgets/input/Number.svelte'
	import Dropdown from '$lib/svelte/widgets/input/Dropdown.svelte'
	import ComboBox from '$lib/svelte/widgets/input/Select.svelte'
	import Select from '$lib/svelte/widgets/input/Select.svelte'
	import Search from '$lib/svelte/widgets/input/Search.svelte'

	export let data
	let { templateComponents, templatePipelines } = data

	templateComponents = templateComponents.map((c) => {
		return { ...c, id: uuidv4() }
	})

	$currentPipelineStore = blankPipeline()
	let settings: Map<string, string>

	let step: number = 0
	const steps: string[] = ['Start', 'Pipeline', 'Components']

	let startService: boolean = true

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

	const modalComponent: ModalComponent = {
		ref: TemplateModal,
		props: { components: [...templateComponents] }
	}

	const modalStore = getModalStore()

	function showTemplateModal() {
		new Promise<DUUIComponent[]>((resolve) => {
			const modal: ModalSettings = {
				type: 'component',
				component: modalComponent,
				response: (components: DUUIComponent[]) => {
					resolve(components)
				}
			}
			modalStore.trigger(modal)
		}).then((response: DUUIComponent[]) => {
			if (!response) return

			response.forEach((component) => {
				const comp = { ...component }
				$currentPipelineStore.components = [...$currentPipelineStore.components, comp]
			})
		})
	}

	const addTemplate = (template: DUUIComponent) => {
		$currentPipelineStore.components = [
			...$currentPipelineStore.components,
			{ ...template, id: uuidv4() }
		]
	}

	const selectPipelineTemplate = (template: DUUIPipeline) => {
		$currentPipelineStore = cloneDeep(template)
		$currentPipelineStore.oid = uuidv4()
		$currentPipelineStore.components = $currentPipelineStore.components.map((c) => {
			return { ...c, id: uuidv4(), index: $currentPipelineStore.components.indexOf(c) }
		})

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
</script>

<div class="container h-full flex-col mx-auto flex gap-4 md:my-8">
	{#if step === 0}
		<h1 class="h2">Choose a starting point</h1>
	{:else}
		<h1 class="h2">{$currentPipelineStore.name || 'New Pipeline'}</h1>
	{/if}

	<hr class="bg-surface-400/20 h-[1px] !border-0 rounded " />
	<div class="grid grid-cols-2 md:flex items-center justify-between gap-4">
		{#if step === 0}
			<ActionButton text="Back" icon={faArrowLeft} on:click={() => goto('/pipelines')} />
			<Search
				style="row-start-2 col-span-2"
				bind:query={searchText}
				icon={faSearch}
				placeholder="Search..."
			/>
		{:else}
			<ActionButton text="Back" icon={faArrowLeft} on:click={() => (step -= 1)} />
		{/if}

		<!-- <div class="hidden md:flex items-center gap-4 md:gap-12 mx-auto">
			{#each steps as s, index}
				<div
					class="flex items-center gap-4 md:gap-12
				{step > index ? 'dark:text-success-400' : step < index ? 'text-stone-400' : ''}"
				>
					<p>{s}</p>
					<Fa class={index === steps.length - 1 ? 'hidden' : ''} icon={faChevronRight} />
				</div>
			{/each}
		</div>
		<p class="h3 mx-auto md:hidden">{steps[step]}</p> -->

		{#if step < steps.length - 1}
			<ActionButton
				text="Continue"
				icon={faArrowRight}
				on:click={() => (step += 1)}
				leftToRight={false}
				_class={$currentPipelineStore.name === '' ? 'invisible' : 'visible'}
			/>
		{:else}
			<ActionButton
				text="Complete"
				icon={faCheck}
				disabled={$currentPipelineStore.components.length === 0}
				variant="variant-filled-success dark:variant-soft-success"
				on:click={createPipeline}
				leftToRight={false}
				_class={$currentPipelineStore.name === '' ? 'invisible' : 'visible'}
			/>
		{/if}
	</div>

	{#if step === 0}
		<div class="grid sm:grid-cols-2 lg:grid-cols-3 gap-4" in:fly={{ x: 300 }} out:fly={{ x: -200 }}>
			<button
				class="text-left shadow-lg p-4 hover:variant-glass bg-surface-100 dark:variant-soft-surface dark:hover:bg-surface-800 space-y-4 flex flex-col"
				on:click={() => selectPipelineTemplate(blankPipeline())}
			>
				<p class="text-lg font-bold">Start from scratch</p>
				<p class="grow">An empty Pipeline.</p>
				<div class="pt-4 flex items-center justify-between gap-4">
					{#each DUUIDrivers as driver}
						<DriverIcon {driver} />
					{/each}
				</div>
			</button>
			{#each filteredTemplatePipelines as pipeline}
				<button
					class="text-left hover:variant-glass shadow-lg dark:variant-soft-surface p-4 bg-surface-100 dark:hover:bg-surface-800 space-y-4 flex flex-col"
					on:click={() => selectPipelineTemplate(pipeline)}
				>
					<p class="text-lg font-bold">{pipeline.name}</p>
					<p class="grow min-h-[50px]">{pipeline.description}</p>
					<div class="pt-4 flex items-center gap-4 justify-between">
						<p>{pipeline.components.length} Component(s)</p>
						<div class="ml-auto flex items-center justify-between gap-4">
							{#each usedDrivers(pipeline) as driver}
								<DriverIcon {driver} />
							{/each}
						</div>
					</div>
				</button>
			{/each}
		</div>
	{:else if step === 1}
		<div in:fly={{ x: 300 }} out:fly={{ x: -200 }} class="space-y-4">
			<div class="bg-surface-100 dark:variant-soft-surface p-4 shadow-lg grid md:grid-cols-3 gap-4">
				<Checkbox
					label="Start service when finished"
					name="start-service"
					bind:checked={startService}
				/>
			</div>
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
					<SettingsMapper bind:settings />
				</div>
			</div>
		</div>
	{:else if step === 2}
		<div in:fly={{ x: 300 }} out:fly={{ x: -200 }} class="space-y-4">
			<div
				class="container space-y-4 bg-surface-100 dark:variant-soft-surface mx-auto shadow-lg p-4"
			>
				{#if $currentPipelineStore.components.length === 0}
					<p class="text-center h4">Add a Component to get Started</p>
				{/if}
				<ul
					use:dndzone={{ items: $currentPipelineStore.components, dropTargetStyle: {} }}
					on:consider={(event) => handleDndConsider(event)}
					on:finalize={(event) => handleDndFinalize(event)}
					class="grid gap-4"
				>
					{#each $currentPipelineStore.components as component (component.id)}
						<div animate:flip={{ duration: flipDurationMs }}>
							<PipelineComponent
								{component}
								on:delete={(event) => {
									$currentPipelineStore.components = $currentPipelineStore.components.filter(
										(c) => c !== event.detail.component
									)
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
				<Fa class="mx-auto" icon={faArrowDown} size="2x" />
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
