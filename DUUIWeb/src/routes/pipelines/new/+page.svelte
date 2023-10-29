<script lang="ts">
	import { goto } from '$app/navigation'
	import ComponentBuilder from '$lib/components/ComponentBuilder.svelte'
	import DriverIcon from '$lib/components/DriverIcon.svelte'
	import { faBookOpen, faEdit, faPlus } from '@fortawesome/free-solid-svg-icons'
	import { Step, Stepper, getModalStore, getToastStore } from '@skeletonlabs/skeleton'
	import { dndzone, type DndEvent } from 'svelte-dnd-action'
	import Fa from 'svelte-fa'
	import { flip } from 'svelte/animate'

	import type { ModalComponent, ModalSettings } from '@skeletonlabs/skeleton'
	import TemplateModal from './TemplateModal.svelte'
	import { componentStore } from './store.js'
	import { invalidNameToast, invalidTargetToast } from './toast.js'
	import { blankPipeline } from '$lib/duui/pipeline'
	import { blankComponent, type DUUIComponent } from '$lib/duui/component'

	export let data
	let { templates } = data

	let pipeline = blankPipeline()
	let editing: boolean = false
	let missing: boolean = false
	let createForm: HTMLFormElement

	const toastStore = getToastStore()

	function handleDndConsider(event: CustomEvent<DndEvent<DUUIComponent>>) {
		pipeline.components = event.detail.items
		pipeline.components = [...pipeline.components]
	}

	function handleDndFinalize(event: CustomEvent<DndEvent<DUUIComponent>>) {
		pipeline.components = event.detail.items
		pipeline.components = [...pipeline.components]
	}

	function editNewComponent() {
		$componentStore = blankComponent(pipeline.id, pipeline.components.length + 1)
		editing = true
	}

	function updateComponent() {
		if ($componentStore.id === '') {
			return
		}

		pipeline.components.forEach((c) => {
			if (c.id === $componentStore.id) {
				c.name = $componentStore.name
				c.categories = $componentStore.categories
				c.description = $componentStore.description
				c.settings.driver = $componentStore.settings.driver
				c.settings.target = $componentStore.settings.target
				c.settings.options = $componentStore.settings.options
			}
		})
		pipeline.components = pipeline.components
	}

	function saveComponent() {
		if ($componentStore.id === '') {
			return
		}

		if (!$componentStore.name) {
			invalidNameToast(toastStore)
			missing = true
		}

		if (!$componentStore.settings.target) {
			invalidTargetToast($componentStore.settings.driver, toastStore)
			missing = true
		}

		if (missing) {
			return
		}

		if (pipeline.components.map((c) => c.id).includes($componentStore.id)) {
			updateComponent()
		} else {
			pipeline.components = [...pipeline.components, { ...$componentStore }]
		}

		editing = false
	}

	function deleteComponent(event: CustomEvent<any>): void {
		pipeline.components = pipeline.components.filter(
			(component: DUUIComponent, index: number, array: DUUIComponent[]) => {
				if (component.id !== event.detail.id) return component
			}
		)

		editing = false
	}

	async function finalizePipeline() {
		await fetch('/pipelines/api/create', {
			method: 'POST',
			headers: {
				'Content-Type': 'application/json'
			},
			body: JSON.stringify(pipeline)
		})
		goto('/pipelines')
	}

	let flipDurationMs = 300

	const modalComponent: ModalComponent = {
		ref: TemplateModal,
		props: { components: [...templates] }
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
				pipeline.components = [...pipeline.components, comp]
			})
		})
	}
</script>

<div class="container h-full mx-auto">
	<Stepper
		on:complete={finalizePipeline}
		class="max-w-7xl mx-auto"
		active="rounded-full variant-filled px-4"
		badge="rounded-full variant-filled-primary"
	>
		<Step locked={pipeline.components.length === 0}>
			<svelte:fragment slot="header">Build Components</svelte:fragment>
			<div
				class="grid lg:grid-cols-3 gap-4 {pipeline.components.length === 0
					? 'grid-cols-1'
					: 'md:grid-cols-2'}"
			>
				<!-- Component List -->
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

				<!-- Component Editor -->
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
				<button class="btn variant-filled-error" on:click={() => goto('/pipelines')}>Cancel</button>
			</svelte:fragment>
		</Step>
		<!-- Pipeline specific settings -->
		<Step>
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
	</Stepper>
</div>
