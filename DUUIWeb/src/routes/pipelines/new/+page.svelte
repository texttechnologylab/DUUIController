<script lang="ts">
	import { goto } from '$app/navigation'
	import DriverIcon from '$lib/components/DriverIcon.svelte'
	import { cloneDeep } from 'lodash'
	import { v4 as uuidv4 } from 'uuid'
	import ComponentModal from '$lib/components/ComponentModal.svelte'
	import {
		faArrowLeft,
		faArrowRight,
		faBookOpen,
		faCheck,
		faChevronRight,
		faChevronUp,
		faFileCircleCheck,
		faPlus
	} from '@fortawesome/free-solid-svg-icons'
	import { getModalStore, getToastStore } from '@skeletonlabs/skeleton'
	import { dndzone, type DndEvent } from 'svelte-dnd-action'
	import Fa from 'svelte-fa'
	import { flip } from 'svelte/animate'

	import type { ModalComponent, ModalSettings } from '@skeletonlabs/skeleton'
	import TemplateModal from './TemplateModal.svelte'
	import { blankPipeline, usedDrivers, type DUUIPipeline } from '$lib/duui/pipeline'
	import { blankComponent, DUUIDrivers, type DUUIComponent } from '$lib/duui/component'
	import { Api, makeApiCall } from '$lib/utils/api'
	import { success } from '$lib/utils/ui'
	import ActionButton from '$lib/components/ActionButton.svelte'
	import PipelineComponent from '$lib/components/PipelineComponent.svelte'
	import IconButton from '$lib/components/IconButton.svelte'
	import { currentPipelineStore } from '$lib/store'
	import SettingsMapper from '$lib/components/SettingsMapper.svelte'

	export let data
	let { templateComponents, templatePipelines } = data

	templateComponents = templateComponents.map((c) => {
		return { ...c, id: uuidv4() }
	})

	$currentPipelineStore = blankPipeline()
	let settings: Map<string, string>

	let step: number = 0
	const steps: string[] = ['Start', 'Pipeline', 'Components']

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

	const showComponentModal = (component: DUUIComponent) => {
		const modal: ModalSettings = {
			type: 'component',
			component: componentModal,
			meta: { component: component }
		}
		modalStore.trigger(modal)
	}

	const addComponent = () => {
		let c = blankComponent($currentPipelineStore.oid, $currentPipelineStore.components.length + 1)
		$currentPipelineStore.components = [...$currentPipelineStore.components, c]
		showComponentModal(c)
	}

	$: {
		if (settings) {
			$currentPipelineStore.settings = settings
		}
	}
</script>

<div class="container h-full flex-col mx-auto flex gap-4 md:my-8">
	{#if step === 0}
		<h1 class="h2">New Pipeline</h1>
	{:else}
		<h1 class="h2">{$currentPipelineStore.name || 'New Pipeline'}</h1>
	{/if}

	<hr />
	<div class="flex items-center justify-start gap-4">
		{#if step === 0}
			<ActionButton text="Back" icon={faArrowLeft} on:click={() => goto('/pipelines')} />
		{:else}
			<ActionButton text="Back" icon={faArrowLeft} on:click={() => (step -= 1)} />
		{/if}

		<div class="steps flex items-center gap-4 mx-auto">
			{#each steps as s, index}
				<div
					class="flex items-center gap-4
				{step > index ? 'text-success-400' : step < index ? 'text-stone-400' : ''}"
				>
					<p>{s}</p>
					<Fa class={index === steps.length - 1 ? 'hidden' : ''} icon={faChevronRight} />
				</div>
			{/each}
		</div>
		{#if step < steps.length - 1}
			<ActionButton
				text="Continue"
				icon={faArrowRight}
				on:click={() => (step += 1)}
				leftToRight={false}
			/>
		{:else}
			<ActionButton text="Complete" icon={faCheck} on:click={createPipeline} leftToRight={false} />
		{/if}
	</div>

	{#if step === 0}
		<h2 class="h4">Choose a starting point</h2>
		<div class="grid sm:grid-cols-2 lg:grid-cols-3 gap-4">
			<button
				class="text-left variant-soft-surface p-4 hover:shadow-lg space-y-4 flex flex-col"
				on:click={() => selectPipelineTemplate(blankPipeline())}
			>
				<p class="text-lg font-bold">Start from scratch</p>
				<p class="grow">An empty Pipeline.</p>
				<div class="pt-4 flex items-center justify-around">
					{#each DUUIDrivers as driver}
						<DriverIcon {driver} />
					{/each}
				</div>
			</button>
			{#each templatePipelines as pipeline}
				<button
					class="text-left variant-soft-surface p-4 hover:shadow-lg space-y-4 flex flex-col"
					on:click={() => selectPipelineTemplate(pipeline)}
				>
					<p class="text-lg font-bold">{pipeline.name}</p>
					<p class="grow">{pipeline.description}</p>
					<div class="pt-4 flex items-center justify-between">
						<p>{pipeline.components.length} Component(s)</p>
						<div class="flex items-center gap-4">
							{#each usedDrivers(pipeline) as driver}
								<DriverIcon {driver} />
							{/each}
						</div>
					</div>
				</button>
			{/each}
		</div>
	{:else if step === 1}
		<div class="grid md:grid-cols-2 gap-4">
			<div class="flex flex-col gap-4 variant-soft-surface rounded-md shadow-lg p-4">
				<label class="label">
					<span>Name</span>
					<input
						bind:value={$currentPipelineStore.name}
						class="border-2 input focus-within:outline-primary-400"
						type="text"
					/>
				</label>

				<label class="label">
					<span>Description</span>

					<textarea
						class="textarea border-2 input"
						rows="4"
						placeholder="Enter a description..."
						bind:value={$currentPipelineStore.description}
					/>
				</label>
			</div>
			<div class="variant-soft-surface p-4 shadow-lg rounded-md">
				<SettingsMapper bind:settings />
			</div>
		</div>
	{:else if step === 2}
		<div class="space-y-4 variant-soft-surface rounded-md shadow-lg p-4">
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
				<IconButton
					icon={faPlus}
					rounded="rounded-full"
					variant="variant-soft-primary"
					on:click={addComponent}
				/>
				<IconButton icon={faBookOpen} rounded="rounded-full" on:click={showTemplateModal} />
			</div>
		</div>
	{/if}

	<!-- <Stepper
		buttonBack="AAA"
		on:complete={createPipeline}
		active="rounded-full variant-filled-primary px-4"
		badge="rounded-full variant-filled"
	>
		<Step locked={pipeline.components.length === 0}>
			<svelte:fragment slot="header">Build Components</svelte:fragment>

			<div
				class="grid lg:grid-cols-3 gap-4 {pipeline.components.length === 0
					? 'grid-cols-1'
					: 'md:grid-cols-2'}"
			>
				{#if pipeline.components.length > 0}
					<aside
						class="rounded-md shadow-lg space-y-4 self-start row-start-2 md:row-start-1 md:col-start-3 md:col-span-1"
					>
						<ul
							use:dndzone={{ items: pipeline.components, dropTargetStyle: {} }}
							on:consider={(event) => handleDndConsider(event)}
							on:finalize={(event) => handleDndFinalize(event)}
							class="grid gap-4 self-stretch"
						>
							{#each pipeline.components as component (component.id)}
								<div
									class="flex items-center justify-start gap-4 card p-4 rounded-md shadow-lg"
									animate:flip={{ duration: flipDurationMs }}
								>
									<DriverIcon driver={component.settings.driver} />
									<p>{component.name}</p>
									<button
										class="btn-icon pointer-events-auto ml-auto variant-soft-primary"
										on:click={() => {
											if (editing && $componentStore.id === component.id) {
												editing = false
											} else {
												editing = true
												$componentStore = { ...component }
											}
										}}
									>
										<Fa icon={faEdit} />
									</button>
								</div>
							{/each}
						</ul>
					</aside>
				{/if}

				{#if editing}
					<div class="card rounded-md shadow-lg md:col-span-2">
						<ComponentBuilder
							on:remove={deleteComponent}
							deleteButton={pipeline.components.map((c) => c.id).includes($componentStore.id)}
						/>

						<div class="grid grid-cols-2 gap-4 p-4">
							<button class="btn variant-filled-success shadow-lg" on:click={saveComponent}>
								<span>Save</span>
							</button>
							<button class="btn variant-filled-error shadow-lg" on:click={() => (editing = false)}>
								<span>Cancel</span>
							</button>
						</div>
					</div>
				{:else}
					<div
						class="container h-full flex-col flex gap-8 justify-center card rounded-md shadow-lg p-4"
					>
						{#if !editing}
							{#if pipeline.components.length === 0}
								<p class="h3 text-center">Start by adding a Component</p>
							{/if}

							<div class="mx-auto grid gap-4">
								<button
									class="btn text-sm md:text-base variant-filled-primary rounded-md shadow-lg"
									on:click={editNewComponent}
								>
									<span>Custom Component</span>
									<Fa icon={faPlus} />
								</button>
								<button
									class="btn text-sm md:text-base variant-ghost-primary rounded-md shadow-lg"
									on:click={showTemplateModal}
								>
									<span>Template Component</span>
									<Fa icon={faBookOpen} />
								</button>
							</div>{/if}
					</div>
				{/if}
			</div>
			<svelte:fragment slot="navigation">
				<ActionButton
					icon={faCancel}
					text="Cancel"
					variant="variant-soft-error"
					on:click={() => goto('/pipelines')}
				/>
			</svelte:fragment>
		</Step>
		<Step buttonBack="btn rounded-md variant-soft-surface">
			<svelte:fragment slot="header">Choose a name for your Pipeline</svelte:fragment>
			<label class="label">
				<span>Name</span>
				<input
					bind:value={pipeline.name}
					class="input focus-within:outline-primary-400"
					type="text"
				/>
			</label>
		</Step>
		<form bind:this={createForm} action="?/create" />
	</Stepper> -->
</div>
