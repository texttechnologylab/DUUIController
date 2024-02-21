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

	import { includes } from '$lib/duui/utils/text'
	import { scrollIntoView, successToast } from '$lib/duui/utils/ui'
	import { currentPipelineStore } from '$lib/store'
	import Chips from '$lib/svelte/components/Chips.svelte'
	import DriverIcon from '$lib/svelte/components/DriverIcon.svelte'
	import JsonInput from '$lib/svelte/components/JsonInput.svelte'
	import PipelineCard from '$lib/svelte/components/PipelineCard.svelte'
	import PipelineComponent from '$lib/svelte/components/PipelineComponent.svelte'
	import Search from '$lib/svelte/components/Search.svelte'
	import TextArea from '$lib/svelte/components/TextArea.svelte'
	import TextInput from '$lib/svelte/components/TextInput.svelte'
	import {
		faArrowDown,
		faArrowLeft,
		faArrowRight,
		faCheck,
		faPlus,
		faSearch,
		faUpload
	} from '@fortawesome/free-solid-svg-icons'
	import type { DrawerSettings } from '@skeletonlabs/skeleton'
	import { getDrawerStore, getToastStore } from '@skeletonlabs/skeleton'
	import pkg from 'lodash'
	import { onMount } from 'svelte'
	import { dndzone, type DndEvent } from 'svelte-dnd-action'
	import Fa from 'svelte-fa'
	import { flip } from 'svelte/animate'
	import { v4 as uuidv4 } from 'uuid'
	import ComponentTemplates from './ComponentTemplates.svelte'
	import { componentDrawerSettings } from '$lib/config'
	const { cloneDeep } = pkg

	export let data

	let { templateComponents, templatePipelines, user } = data

	templateComponents = templateComponents.map((c) => {
		return { ...c, id: uuidv4() }
	})

	let step: number = +($page.url.searchParams.get('step') || '0')

	let settings: Map<string, string>

	if (step === 0) {
		$currentPipelineStore = blankPipeline()
	} else {
		settings = new Map(Object.entries($currentPipelineStore.settings))
		$currentPipelineStore.components.forEach((component) => {
			component.oid = uuidv4()
			component.id = uuidv4()
			component.pipeline_id = $currentPipelineStore.oid
		})
	}

	let searchText: string = ''
	let filteredTemplatePipelines: DUUIPipeline[] = templatePipelines
	let loaded: boolean = false

	onMount(() => {
		loaded = true
	})

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

		if (settings) {
			$currentPipelineStore.settings = settings
		}

		if (loaded) {
			step, scrollIntoView('top')
		}
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
			await fetch('/api/pipelines', {
				method: 'PUT',
				body: JSON.stringify({
					oid: selectedTemplate.oid,
					timesUsed: selectedTemplate.times_used + 1
				})
			})
		}

		$currentPipelineStore.settings = Object.fromEntries(settings.entries())
		const response = await fetch('/api/pipelines', {
			method: 'POST',
			body: JSON.stringify($currentPipelineStore)
		})

		if (response.ok) {
			const data = await response.json()
			toastStore.trigger(successToast('Pipeline created successfully'))
			goto(`/pipelines/${data.oid}`)
		}
	}

	let flipDurationMs = 300
	let selectedTemplate: DUUIPipeline | null

	const addTemplate = (template: DUUIComponent) => {
		const copyTemplate = cloneDeep(template)
		copyTemplate.id = uuidv4()
		copyTemplate.index = $currentPipelineStore.components.length
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

		step = 1
		goto(`/pipelines/build?step=${step}`)
	}

	const drawerStore = getDrawerStore()
	const drawer: DrawerSettings = {
		id: 'component',
		...componentDrawerSettings,
		meta: {
			component: blankComponent($currentPipelineStore.oid, $currentPipelineStore.components.length),
			inEditor: true,
			creating: true
		}
	}

	const uploadPipeline = async () => {
		$currentPipelineStore.settings = Object.fromEntries(settings.entries())
		const response = await fetch('/api/pipelines?template=true', {
			method: 'POST',
			body: JSON.stringify($currentPipelineStore)
		})

		if (response.ok) {
			toastStore.trigger(successToast('Pipeline uploaded successfully'))
			goto(`/pipelines`)
		}
	}
</script>

<div class="menu-mobile">
	<button
		class="button-mobile"
		on:click={() => {
			if (step == 0) {
				goto('/pipelines')
			} else {
				step -= 1
				goto(`/pipelines/build?step=${step}`)

				scrollIntoView('top')
			}
		}}
	>
		<Fa icon={faArrowLeft} />
		<span>{step === 0 ? 'Back to Dashboard' : step === 1 ? 'Back' : 'Settings'}</span>
	</button>
	{#if user?.role === 'Admin' && step === 2 && $currentPipelineStore.components.length !== 0}
		<button class="button-mobile" on:click={uploadPipeline}>
			<Fa icon={faUpload} />
			<span>Publish as Template</span>
		</button>
	{/if}
	<button
		class="button-mobile"
		on:click={() => {
			if (step === 2) {
				createPipeline()
			} else {
				step += 1
				goto(`/pipelines/build?step=${step}`)
				scrollIntoView('top')
			}
		}}
		disabled={(step === 1 && !$currentPipelineStore.name) ||
			(step === 2 && $currentPipelineStore.components.length === 0)}
	>
		<Fa icon={step === 2 ? faCheck : faArrowRight} />
		<span>{step === 0 ? 'Start from scratch' : step === 1 ? 'Next' : 'Finish'}</span>
	</button>
</div>

<div class="h-full gradient bg-repeat">
	<div class="sticky top-0 bg-surface-50-900-token border-y p-4 border-color hidden md:block z-10">
		<div class="grid grid-cols-2 md:flex items-center md:justify-between gap-4 relative">
			<button
				class="button-neutral"
				on:click={() => {
					if (step == 0) {
						goto('/pipelines')
					} else {
						step -= 1
						goto(`/pipelines/build?step=${step}`)

						scrollIntoView('top')
					}
				}}
			>
				<Fa icon={faArrowLeft} />
				<span>Back</span>
			</button>
			{#if user?.role === 'Admin' && step === 2 && $currentPipelineStore.components.length !== 0}
				<button class="col-span-2 button-success md:ml-auto row-start-2" on:click={uploadPipeline}>
					<Fa icon={faUpload} />
					<span>Publish as Template</span>
				</button>
			{/if}
			<button
				class={step === 2 ? 'button-success' : 'button-primary'}
				on:click={() => {
					if (step === 2) {
						createPipeline()
					} else {
						step += 1
						goto(`/pipelines/build?step=${step}`)

						scrollIntoView('top')
					}
				}}
				disabled={(step === 1 && !$currentPipelineStore.name) ||
					(step === 2 && $currentPipelineStore.components.length === 0)}
			>
				<span>{step <= 1 ? 'Next' : 'Finish'}</span>
				<Fa icon={step === 2 ? faCheck : faArrowRight} />
			</button>
		</div>
	</div>

	<div class="p-4 space-y-8 pb-20">
		{#if step === 0}
			<h1 class="h2">Start from scratch</h1>
			<div class="space-y-8">
				<div class="grid md:grid-cols-2 xl:grid-cols-3 gap-4 md:gap-8">
					<button
						class="card-fancy text-left grid items-start min-h-[300px]"
						on:click={() => selectPipelineTemplate()}
					>
						<div class="flex items-center gap-4 justify-between">
							<p class="text-lg font-bold">Start from scratch</p>
							<p class="badge variant-filled-warning">New</p>
						</div>

						<p class="row-span-2">A new Pipeline without any predefined settings.</p>
						<div class="flex items-center justify-end gap-4 self-end">
							{#each DUUIDrivers as driver}
								<DriverIcon {driver} />
							{/each}
						</div>
					</button>
				</div>
				<hr class="hr !my-16" />
				<div class="space-y-4">
					<div class="md:flex justify-between items-end space-y-4">
						<h2 class="h2 font-bold">Templates</h2>
						<div class="grid gap-4">
							<!-- <Dropdown
									name="driverFilter"
									placement="bottom-end"
									options={DUUIDriverFilters}
									bind:value={driverFilter}
								/> -->

							<Search bind:query={searchText} icon={faSearch} placeholder="Search..." />
						</div>
					</div>
					<div id="templates" class="grid gap-4 md:gap-8 md:grid-cols-2 xl:grid-cols-3">
						{#each filteredTemplatePipelines as pipeline}
							<button
								class="card-fancy text-left grid min-h-[300px] items-start"
								on:click={() => selectPipelineTemplate(pipeline)}
							>
								<PipelineCard {pipeline} />
							</button>
						{/each}
					</div>
				</div>
			</div>
		{:else if step === 1}
			<h1 class="h1 text-center my-4">
				{$currentPipelineStore.name || 'New Pipeline'}
			</h1>
			<div class="space-y-8">
				<div class="grid md:grid-cols-2 gap-4">
					<div
						class="section-wrapper flex flex-col gap-4 p-4 {$currentPipelineStore.name !== ''
							? ''
							: 'border-error-400'}"
					>
						<TextInput
							label="Name"
							error={$currentPipelineStore.name === '' ? 'The name can not be empty' : ''}
							name="pipeline-name"
							bind:value={$currentPipelineStore.name}
						/>

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
					<div class="section-wrapper p-4 relative grid">
						<JsonInput label="Settings" bind:data={settings} />
						<!-- <SettingsMapper bind:map={settings} /> -->
					</div>
				</div>
			</div>
		{:else if step === 2}
			<div class="space-y-8">
				<div class="min-h-[400px] space-y-8 isolate md:p-16">
					{#if $currentPipelineStore.components.length === 0}
						<p class="mx-auto text-center h2 font-bold">Add a Component to get Started</p>
					{/if}
					<ul
						use:dndzone={{ items: $currentPipelineStore.components, dropTargetStyle: {} }}
						on:consider={(event) => handleDndConsider(event)}
						on:finalize={(event) => handleDndFinalize(event)}
						class="grid max-w-5xl mx-auto !cursor-move"
					>
						{#each $currentPipelineStore.components as component (component.id)}
							<div animate:flip={{ duration: flipDurationMs }} class="relative !cursor-move">
								<PipelineComponent
									{component}
									inEditor={true}
									on:remove={(event) => {
										$currentPipelineStore.components = $currentPipelineStore.components.filter(
											(c) => c.id !== event.detail.id
										)
									}}
								/>
								{#if component.index < $currentPipelineStore.components.length - 1}
									<div
										class="my-4 mx-auto flex items-center justify-center
											relative
											before:absolute before:h-full before:w-1 before:-translate-x-1/2 before:left-1/2
											before:bg-surface-100-800-token before:-z-50 before:scale-y-[200%]
											"
									>
										<button
											class="button-neutral bg-surface-100-800-token !aspect-square !rounded-full !p-3"
											on:click={() => drawerStore.open(drawer)}
										>
											<Fa icon={faPlus} />
										</button>
									</div>
								{/if}
							</div>
						{/each}
					</ul>
					<div class="mx-auto flex items-center justify-center">
						<button
							class="button-neutral !aspect-square !rounded-full bg-surface-100-800-token !p-3"
							on:click={() => drawerStore.open(drawer)}
						>
							<Fa icon={faPlus} />
						</button>
					</div>

					<div class="flex flex-col items-center gap-8">
						<p class="text-center">Or choose a template</p>
						<a
							href="/pipelines/build#component-templates"
							class="flex justify-center text-center transition-colors duration-300 dimmed hover:text-primary-500"
						>
							<Fa icon={faArrowDown} size="3x" />
						</a>
					</div>
				</div>
				<hr class="hr !my-16" />
				<div id="component-templates" class="scroll-mt-8 md:scroll-mt-24">
					<ComponentTemplates
						components={templateComponents}
						on:select={(event) => addTemplate(event.detail.component)}
					/>
				</div>
			</div>
		{/if}
	</div>
</div>
