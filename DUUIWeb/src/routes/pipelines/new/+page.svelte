<script lang="ts">
	import { goto } from '$app/navigation'
	import ComponentBuilder from '$lib/components/ComponentBuilder.svelte'
	import DriverIcon from '$lib/components/DriverIcon.svelte'
	import { blankComponent, blankPipeline, type DUUIPipelineComponent } from '$lib/data'
	import { faArrowRight, faBookOpen, faEdit, faPlus } from '@fortawesome/free-solid-svg-icons'
	import { getModalStore, getToastStore, Step, Stepper } from '@skeletonlabs/skeleton'
	import { dndzone, type DndEvent } from 'svelte-dnd-action'
	import Fa from 'svelte-fa'
	import { flip } from 'svelte/animate'

	import type { ModalComponent, ModalSettings, ToastSettings } from '@skeletonlabs/skeleton'
	import { componentStore } from './store.js'
	import { invalidNameToast, invalidTargetToast } from './toast.js'
	import TemplateModal from './TemplateModal.svelte'

	export let data
	let { templates } = data

	let pipeline = blankPipeline()
	let editing: boolean = false
	let missing: boolean = false
	let createForm: HTMLFormElement

	function handleDndConsider(event: CustomEvent<DndEvent<DUUIPipelineComponent>>) {
		pipeline.components = event.detail.items
		pipeline.components = [...pipeline.components]
	}

	function handleDndFinalize(event: CustomEvent<DndEvent<DUUIPipelineComponent>>) {
		pipeline.components = event.detail.items
		pipeline.components = [...pipeline.components]
	}

	function editNewComponent() {
		$componentStore = blankComponent(pipeline.components.length + 1)
		editing = true
	}

	function editComponent(id: number) {
		$componentStore = { ...pipeline.components[id] }
		editing = true
	}

	function updateComponent() {
		if ($componentStore.id === -1) {
			return
		}

		pipeline.components.forEach((c) => {
			if (c.id === $componentStore.id) {
				c.name = $componentStore.name
				c.category = $componentStore.category
				c.description = $componentStore.description
				c.settings.driver = $componentStore.settings.driver
				c.settings.target = $componentStore.settings.target
			}
		})
		pipeline.components = pipeline.components
	}

	function saveComponent() {
		if ($componentStore.id === -1) {
			return
		}

		if (!$componentStore.name) {
			invalidNameToast()
			missing = true
		}

		if (!$componentStore.settings.target) {
			invalidTargetToast($componentStore.settings.driver)
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
			(component: DUUIPipelineComponent, index: number, array: DUUIPipelineComponent[]) => {
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
		props: { components: templates }
	}

	const modalStore = getModalStore()
	function showTemplateModal() {
		new Promise<DUUIPipelineComponent[]>((resolve) => {
			const modal: ModalSettings = {
				type: 'component',
				component: modalComponent,
				response: (components: DUUIPipelineComponent[]) => {
					resolve(components)
				}
			}
			modalStore.trigger(modal)
		}).then((response: DUUIPipelineComponent[]) => {
			if (!response) {
				return
			}
			response.forEach((component) => {
				const comp = { ...component }
				comp.id = pipeline.components.length + 1
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
			<div class="grid grid-cols-3 gap-4">
				<!-- Component List -->
				<aside class="variant-soft-surface p-4 space-y-4 self-start">
					{#if pipeline.components.length === 0}
						<div class="p-4 space-y-4 flex flex-col items-center justify-center">
							<p class="h5">Start by adding a new Component</p>
							<Fa size="3x" icon={faArrowRight} />
						</div>
					{:else}
						<ul
							use:dndzone={{ items: pipeline.components, dropTargetStyle: {} }}
							on:consider={(event) => handleDndConsider(event)}
							on:finalize={(event) => handleDndFinalize(event)}
							class="grid gap-4 self-stretch"
						>
							{#each pipeline.components as component (component.id)}
								<div
									class="flex items-center justify-start gap-4 card p-4"
									animate:flip={{ duration: flipDurationMs }}
								>
									<DriverIcon driver={component.settings.driver} />
									<p>{component.name}</p>
									<button
										class="btn-icon pointer-events-auto ml-auto"
										on:click={() => {
											if (editing && $componentStore.id === component.id) {
												editing = false
											} else {
												editing = true
												$componentStore = { ...component }
											}
										}}
									>
										<span>
											<Fa size="lg" icon={faEdit} />
										</span>
									</button>
								</div>
							{/each}
						</ul>
					{/if}
				</aside>

				<!-- Component Editor -->
				<div class="container h-full flex-col flex gap-4 col-span-2 justify-center">
					{#if editing}
						<ComponentBuilder
							on:remove={deleteComponent}
							deleteButton={pipeline.components.map((c) => c.id).includes($componentStore.id)}
						/>
					{/if}
					{#if !editing}
						<div class="mx-auto grid grid-cols-2 gap-4">
							<button class="btn variant-filled-primary" on:click={editNewComponent}>
								<span>New Component</span>
								<Fa icon={faPlus} />
							</button>
							<button class="btn variant-ghost-primary" on:click={showTemplateModal}>
								<span>Choose template</span>
								<Fa icon={faBookOpen} />
							</button>
						</div>
					{:else}
						<div class="mr-auto grid grid-cols-2 gap-4">
							<button
								class="btn variant-filled-success rounded-sm shadow-lg"
								on:click={saveComponent}
							>
								<span>Save</span>
							</button>
							<button
								class="btn variant-filled-error rounded-sm shadow-lg"
								on:click={() => (editing = false)}
							>
								<span>Cancel</span>
							</button>
						</div>
					{/if}
				</div>
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
