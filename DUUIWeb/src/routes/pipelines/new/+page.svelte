<script lang="ts">
	import { goto } from '$app/navigation'
	import ComponentBuilder from '$lib/components/ComponentBuilder.svelte'
	import DriverIcon from '$lib/components/DriverIcon.svelte'
	import PipelineComponent from '$lib/components/PipelineComponent.svelte'
	import { blankComponent, blankPipeline, DUUIDrivers, type DUUIPipelineComponent } from '$lib/data'
	import { faAdd, faFilter } from '@fortawesome/free-solid-svg-icons'
	import { Step, Stepper } from '@skeletonlabs/skeleton'
	import { dndzone, type DndEvent } from 'svelte-dnd-action'
	import Fa from 'svelte-fa'
	import { flip } from 'svelte/animate'

	import { popup } from '@skeletonlabs/skeleton'
	import type { PopupSettings } from '@skeletonlabs/skeleton'

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
		pipeline.components = [
			...pipeline.components,
			blankComponent(pipeline.components.length + 1 + '')
		]
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
		<Step locked={pipeline.name === ''}>
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
		</Step>

		<Step locked={pipeline.components.length === 0}>
			<svelte:fragment slot="header">Create Components</svelte:fragment>
			<div class="flex">
				<aside class="card space-y-4 p-4 variant-outline-primary">
					{#each templates as template}
						<div class="flex flex-col items-start justify-center gap-4 card p-4">
							<header class="flex justify-between items-center gap-4">
								<DriverIcon driver={template.driver} />
								<p>{template.name}</p>
							</header>
						</div>
					{/each}
				</aside>
				<div class="container h-full flex-col items-center mx-auto flex gap-4 py-8">
					<ul
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
					</ul>
					<button class="btn-icon variant-filled-primary" on:click={addComponent}>
						<Fa icon={faAdd} />
					</button>
				</div>
			</div>
		</Step>
		<!-- ... -->
	</Stepper>
</div>
