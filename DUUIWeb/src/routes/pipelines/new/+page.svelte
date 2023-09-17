<script lang="ts">
	import { goto } from '$app/navigation'
	import ComponentBuilder from '$lib/components/ComponentBuilder.svelte'
	import DriverIcon from '$lib/components/DriverIcon.svelte'
	import PipelineComponent from '$lib/components/PipelineComponent.svelte'
	import {
		blankComponent,
		blankPipeline,
		DUUIDrivers,
		DUUIRemoteDriver,
		type DUUIPipelineComponent,
		DUUIUIMADriver
	} from '$lib/data'
	import {
		faAdd,
		faArrowRight,
		faArrowRightLong,
		faEdit,
		faFilter
	} from '@fortawesome/free-solid-svg-icons'
	import { getToastStore, Step, Stepper } from '@skeletonlabs/skeleton'
	import { dndzone, type DndEvent } from 'svelte-dnd-action'
	import Fa from 'svelte-fa'
	import { flip } from 'svelte/animate'

	import { popup } from '@skeletonlabs/skeleton'
	import type { PopupSettings, ToastSettings } from '@skeletonlabs/skeleton'
	import ComponentEditor from '$lib/components/ComponentEditor.svelte'

	export let data
	let { templates } = data

	let pipeline = blankPipeline()

	function handleDndConsider(event: CustomEvent<DndEvent<DUUIPipelineComponent>>) {
		pipeline.components = event.detail.items
		pipeline.components = [...pipeline.components]
	}

	function handleDndFinalize(event: CustomEvent<DndEvent<DUUIPipelineComponent>>) {
		pipeline.components = event.detail.items
		pipeline.components = [...pipeline.components]
	}

	function addComponent() {
		editedComponent = blankComponent(pipeline.components.length + 1 + '')
	}

	let exception: string = ''

	function saveComponent() {
		if (editedComponent === undefined) {
			return
		}

		if (!editedComponent.target) {
			const t: ToastSettings = {
				message:
					(editedComponent.driver === DUUIRemoteDriver
						? 'Target'
						: editedComponent.driver === DUUIUIMADriver
						? 'Class Path'
						: 'Image Name') + ' cannot be empty!',
				timeout: 4000,
				background: 'variant-filled-error'
			}
			exception = 'target'
			getToastStore().trigger(t)
			return
		}

		pipeline.components = [...pipeline.components, editedComponent]

		editedComponent = undefined
	}

	function deleteComponent(event: CustomEvent<any>): void {
		pipeline.components = pipeline.components.filter(
			(component: DUUIPipelineComponent, index: number, array: DUUIPipelineComponent[]) => {
				if (component.id !== event.detail.id) return component
			}
		)
	}

	async function finalizePipeline() {
		console.log(JSON.stringify(pipeline))

		const result = await fetch('http://127.0.0.1:2605/pipelines', {
			method: 'POST',
			mode: 'cors',
			body: JSON.stringify(pipeline)
		})

		const status = await result.json()
		console.log(status)

		if (status.message === 'success') {
			goto('/pipelines/' + status.id)
		}
	}

	let flipDurationMs = 300

	let searchText: string = ''
	let searchOpen: boolean = false
	let filteredTemplates = templates
	let editedComponent: DUUIPipelineComponent | undefined

	$: {
		if (searchText === '') {
			filteredTemplates = templates
		} else {
			filteredTemplates = templates.filter((template) => {
				if (template.name.toLowerCase().includes(searchText.toLowerCase())) {
					return template
				}
			})
		}
	}
</script>

<div class="container h-full mx-auto">
	<Stepper on:complete={finalizePipeline} class="max-w-7xl mx-auto">
		<!-- <Step locked={pipeline.name === ''}>
			<svelte:fragment slot="header">Choose a name for your Pipeline</svelte:fragment>
			<label class="label">
				<span>Name</span>
				<input
					bind:value={pipeline.name}
					class="input focus-within:outline-primary-400"
					type="text"
				/>
			</label>
			<svelte:fragment slot="navigation">
				<button class="btn variant-filled-error" on:click={() => goto('/pipelines')}>Cancel</button>
			</svelte:fragment>
		</Step> -->

		<Step locked={pipeline.components.length === 0}>
			<svelte:fragment slot="header">{pipeline.name}</svelte:fragment>
			<div class="grid grid-cols-3 gap-16">
				<aside class="variant-soft-surface p-4 space-y-4 self-start">
					{#if pipeline.components.length === 0}
						<div class="p-4 space-y-4 flex flex-col items-center justify-center">
							<p>Start by adding a new Component</p>
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
									<DriverIcon driver={component.driver} />
									<p>{component.name}</p>
									{#if editedComponent?.id !== component.id}
										<button
											class="btn-icon pointer-events-auto variant-glass-primary ml-auto"
											on:click={() => (editedComponent = component)}
										>
											<span>
												<Fa size="md" icon={faEdit} />
											</span>
										</button>
									{/if}
								</div>
							{/each}
						</ul>
					{/if}
				</aside>
				<div class="container h-full flex-col flex gap-4 col-span-2">
					{#if editedComponent}
						<ComponentBuilder
							component={editedComponent}
							{exception}
							on:remove={deleteComponent}
							on:save={saveComponent}
						/>
					{/if}
					<!-- <ul
						use:dndzone={{ items: pipeline.components, dropTargetStyle: {} }}
						on:consider={(event) => handleDndConsider(event)}
						on:finalize={(event) => handleDndFinalize(event)}
						class="grid gap-4 self-stretch"
					>
						{#each pipeline.components as component (component.id)}
							<div animate:flip={{ duration: flipDurationMs }}>
								<ComponentBuilder {component} on:remove={deleteComponent} />
							</div>
						{/each}
					</ul> -->
					<div class="mx-auto">
						{#if !editedComponent}
							<button class="btn variant-ghost-primary" on:click={addComponent}>
								<span>New Component</span>
							</button>
						{/if}
						{#if editedComponent}
							<div class="mx-auto space-x-4 grid grid-cols-2">
								<button class="btn variant-ghost-success" on:click={saveComponent}>
									<span>Save Component</span>
								</button>
								<button
									class="btn variant-ghost-error"
									on:click={() => {
										editedComponent = undefined
										exception = ''
									}}
								>
									<span>Cancel</span>
								</button>
							</div>
						{/if}
					</div>
				</div>
			</div>
		</Step>
		<!-- ... -->
	</Stepper>
</div>
