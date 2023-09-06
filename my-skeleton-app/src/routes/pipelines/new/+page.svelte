<script lang="ts">
	import { goto } from '$app/navigation'
	import PipelineComponent from '$lib/components/PipelineComponent.svelte'
	import { blankComponent, blankPipeline, type DUUIPipelineComponent } from '$lib/data'
	import { faAdd } from '@fortawesome/free-solid-svg-icons'
	import { Step, Stepper } from '@skeletonlabs/skeleton'
	import { dndzone, type DndEvent } from 'svelte-dnd-action'
	import Fa from 'svelte-fa'
	import { flip } from 'svelte/animate'

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
		pipeline.components = [...pipeline.components, blankComponent(pipeline.id)]
	}

	function deleteComponent(event: CustomEvent<any>): void {
		pipeline.components = pipeline.components.filter(
			(component: DUUIPipelineComponent, index: number, array: DUUIPipelineComponent[]) => {
				if (component.id !== event.detail.id) return component
			}
		)
	}

	async function finalizePipeline() {
		const result = await fetch('http://192.168.2.122:2605/pipeline', {
			method: 'POST',
			mode: 'cors',
			body: JSON.stringify(pipeline)
		})

		const status = await result.json()
		console.log(status);
		
		if (status.message === 'success') {
			goto('/pipelines/' + status.id)
		}
	}

	let flipDurationMs = 300
</script>

<Stepper on:complete={finalizePipeline}>
	<Step locked={pipeline.name === ''}>
		<svelte:fragment slot="header">Choose a name</svelte:fragment>
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
		<div class="container h-full flex-col items-center mx-auto flex gap-4 py-8">
			<ul
				use:dndzone={{ items: pipeline.components, dropTargetStyle: {} }}
				on:consider={(event) => handleDndConsider(event)}
				on:finalize={(event) => handleDndFinalize(event)}
				class="grid gap-4 self-stretch"
			>
				{#each pipeline.components as component (component.id)}
					<div animate:flip={{ duration: flipDurationMs }}>
						<PipelineComponent
							editMode={true}
							{component}
							on:deletion={deleteComponent}
							on:remove={deleteComponent}
						/>
					</div>
				{/each}
			</ul>
			<button class="btn-icon variant-filled-primary" on:click={addComponent}
				><Fa icon={faAdd} /></button
			>
		</div>
	</Step>
	<!-- ... -->
</Stepper>
