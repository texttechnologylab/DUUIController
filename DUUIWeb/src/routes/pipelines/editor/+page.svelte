<script lang="ts">
	import { goto } from '$app/navigation'
	import { page } from '$app/stores'
	import { DUUIDrivers, blankComponent, type DUUIComponent } from '$lib/duui/component'
	import {
		blankPipeline,
		getPipelineCategories,
		usedDrivers,
		type DUUIPipeline
	} from '$lib/duui/pipeline'
	import { Api, makeApiCall } from '$lib/duui/utils/api'
	import { includes } from '$lib/duui/utils/text'
	import { scrollIntoView, successToast } from '$lib/duui/utils/ui'
	import { currentPipelineStore } from '$lib/store'
	import DriverIcon from '$lib/svelte/DriverIcon.svelte'
	import ActionButton from '$lib/svelte/widgets/action/ActionButton.svelte'
	import PipelineCard from '$lib/svelte/widgets/duui/PipelineCard.svelte'
	import PipelineComponent from '$lib/svelte/widgets/duui/PipelineComponent.svelte'
	import Chips from '$lib/svelte/widgets/input/Chips.svelte'
	import JsonPreview from '$lib/svelte/widgets/input/JsonInput.svelte'
	import Search from '$lib/svelte/widgets/input/Search.svelte'
	import TextArea from '$lib/svelte/widgets/input/TextArea.svelte'
	import Text from '$lib/svelte/widgets/input/TextInput.svelte'
	import {
		faArrowDown,
		faArrowLeft,
		faArrowRight,
		faCheck,
		faPlus,
		faSearch,
		faUpload
	} from '@fortawesome/free-solid-svg-icons'
	import type { ModalSettings } from '@skeletonlabs/skeleton'
	import { getModalStore, getToastStore } from '@skeletonlabs/skeleton'
	import { dndzone, type DndEvent } from 'svelte-dnd-action'
	import Fa from 'svelte-fa'
	import { flip } from 'svelte/animate'
	import { v4 as uuidv4 } from 'uuid'
	import ComponentTemplates from './ComponentTemplates.svelte'
	import pkg from 'lodash'
	const { cloneDeep } = pkg

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

	let searchText: string = ''
	let filteredTemplatePipelines: DUUIPipeline[] = templatePipelines

	$: {
		filteredTemplatePipelines = templatePipelines.filter(
			(pipeline: DUUIPipeline) =>
				includes(
					`${pipeline.name} ${pipeline.description} ${getPipelineCategories(pipeline)} ${Array.from(
						usedDrivers(pipeline)
					).join(' ')}`,
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
		if (selectedTemplate) {
			await fetch('/pipelines/api', {
				method: 'PUT',
				body: JSON.stringify({
					oid: selectedTemplate.oid,
					timesUsed: selectedTemplate.timesUsed + 1
				})
			})
		}

		$currentPipelineStore.settings = Object.fromEntries(settings.entries())
		const response = await makeApiCall(Api.Pipelines, 'POST', { pipeline: $currentPipelineStore })

		if (response.ok) {
			const data = await response.json()
			toastStore.trigger(successToast('Pipeline created successfully'))
			goto(`/pipelines/${data.oid}`)
		}
	}

	let flipDurationMs = 300
	let selectedTemplate: DUUIPipeline | null

	const modalStore = getModalStore()

	const addTemplate = (template: DUUIComponent) => {
		const copyTemplate = cloneDeep(template)
		copyTemplate.id = uuidv4()

		$currentPipelineStore.components = [...$currentPipelineStore.components, copyTemplate]
		scrollIntoView('top')
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

	const addComponent = () => {
		let c = blankComponent($currentPipelineStore.oid, $currentPipelineStore.components.length + 1)
		new Promise<{ accepted: boolean; component: DUUIComponent }>((resolve) => {
			const modal: ModalSettings = {
				type: 'component',
				component: 'componentModal',
				meta: { component: c, new: true },
				response: (r: { accepted: boolean; component: DUUIComponent }) => {
					resolve(r)
				}
			}
			modalStore.trigger(modal)
		}).then(async ({ accepted, component }) => {
			if (!accepted) return
			$currentPipelineStore.components = [...$currentPipelineStore.components, c]
		})
	}

	const uploadPipeline = async () => {
		$currentPipelineStore.settings = Object.fromEntries(settings.entries())
		const response = await makeApiCall(Api.Pipelines, 'POST', {
			pipeline: $currentPipelineStore,
			template: true
		})

		if (response.ok) {
			toastStore.trigger(successToast('Pipeline uploaded successfully'))
			goto(`/pipelines`)
		}
	}
	$: {
		if (settings) {
			$currentPipelineStore.settings = settings
		}
	}

	$: step, scrollIntoView('top')
</script>

<div class="h-full">
	<div class="grid">
		<div
			class="page-wrapper bg-solid md:top-0 z-10 left-0 bottom-0 right-0 row-start-2 fixed md:sticky md:row-start-1 {step ===
			0
				? 'md:hidden'
				: ''}"
		>
			<div class="grid grid-cols-2 md:flex items-center md:justify-between gap-4 relative">
				<button
					class="button-primary"
					on:click={() => (step === 0 ? goto('/pipelines') : (step -= 1))}
				>
					<Fa icon={faArrowLeft} />
					<span>{step === 0 ? 'Pipelines' : step === 1 ? 'Start' : 'Settings'}</span>
				</button>
				{#if user?.role === 'admin' && step === 2 && $currentPipelineStore.components.length !== 0}
					<button
						class="col-span-2 button-success md:ml-auto row-start-2"
						on:click={uploadPipeline}
					>
						<Fa icon={faUpload} />
						<span>Publish as Template</span>
					</button>
				{/if}
				<button
					class={step === 2 ? 'button-success' : 'button-primary'}
					on:click={() => (step === 2 ? createPipeline() : (step += 1))}
					disabled={(step === 1 && !$currentPipelineStore.name) ||
						(step === 2 && $currentPipelineStore.components.length === 0)}
				>
					<span>{step === 0 ? 'Settings' : step === 1 ? 'Components' : 'Finish'}</span>
					<Fa icon={step === 2 ? faCheck : faArrowRight} />
				</button>
			</div>
		</div>

		<div class="p-4 md:p-8 space-y-8">
			<h1 class="h2 font-bold">
				{step === 0 ? 'Start from scratch' : $currentPipelineStore.name || 'New Pipeline'}
			</h1>

			{#if step === 0}
				<div class="space-y-8">
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
					<hr class="hr !my-16 !md:block !hidden" />
					<div class="space-y-8">
						<div class="md:flex justify-between items-end space-y-4">
							<h2 class="h2 font-bold">Templates</h2>
							<Search
								style="row-start-2 col-span-2"
								bind:query={searchText}
								icon={faSearch}
								placeholder="Search..."
							/>
						</div>
						<div class="grid md:grid-cols-2 xl:grid-cols-3 gap-4">
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
				<div class="space-y-8">
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
							<Chips
								label="Tags"
								placeholder="Add a tag..."
								bind:values={$currentPipelineStore.tags}
							/>
						</div>
						<div class="section-wrapper px-4 py-2 relative">
							<JsonPreview bind:data={settings} />
							<!-- <SettingsMapper bind:map={settings} /> -->
						</div>
					</div>
				</div>
			{:else if step === 2}
				<div class="space-y-8">
					<div
						class="rounded-md border border-surface-200 space-y-8
								dark:border-surface-500 isolate bg-surface-100 dark:variant-soft-surface shadow-lg p-4 md:p-16"
					>
						{#if $currentPipelineStore.components.length === 0}
							<p class="text-center h2 font-bold">Add a Component to get Started</p>
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
												(c) => c.id !== event.detail.id
											)
										}}
									/>
								</div>
							{/each}
						</ul>
						<div class="mx-auto flex items-center justify-center">
							<ActionButton
								text="Add"
								icon={faPlus}
								variant="variant-filled-primary dark:variant-soft-primary"
								on:click={addComponent}
							/>
						</div>

						<div class="flex flex-col items-center gap-8">
							<p class="text-center">Or choose a template</p>
							<a
								href="/pipelines/editor#templates"
								class="flex justify-center text-center transition-colors duration-300 text-surface-400 hover:text-primary-500"
							>
								<Fa icon={faArrowDown} size="3x" />
							</a>
						</div>
					</div>
					<hr class="hr !my-16" />
					<div id="templates" class="scroll-mt-8 md:scroll-mt-24">
						<ComponentTemplates
							components={templateComponents}
							on:select={(event) => addTemplate(event.detail.component)}
						/>
					</div>
				</div>
			{/if}
		</div>
	</div>
</div>
