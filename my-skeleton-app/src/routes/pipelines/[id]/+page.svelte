<script lang="ts">
	import PipelineComponent from '$lib/components/PipelineComponent.svelte'
	import type { DUUIPipelineComponent } from '$lib/data'
	import { currentPipelineStore } from '$lib/store'
	import { dndzone, type DndEvent } from 'svelte-dnd-action'
	import { flip } from 'svelte/animate'
	import { v4 as uuidv4 } from 'uuid'

	function handleDndConsider(event: CustomEvent<DndEvent<DUUIPipelineComponent>>) {
		$currentPipelineStore.components = event.detail.items
		$currentPipelineStore.components = [...$currentPipelineStore.components]
	}

	function handleDndFinalize(event: CustomEvent<DndEvent<DUUIPipelineComponent>>) {
		$currentPipelineStore.components = event.detail.items
		$currentPipelineStore.components = [...$currentPipelineStore.components]
	}

	let flipDurationMs = 300

	$currentPipelineStore.components.forEach((component) => {
		if (component.id === undefined) {
			component.id = uuidv4()
		}
	})

	function deleteComponent(e: CustomEvent<any>): void {
		throw new Error('Function not implemented.')
	}
</script>

<div class="flex flex-col">
	<div class="grow p-4">
		<p class="h2 font-bold">{$currentPipelineStore.name}</p>

		{#if $currentPipelineStore.status === 'Error'}
			<p class="text-error-400">{$currentPipelineStore.status}</p>
		{:else if $currentPipelineStore.status === 'Cancelled'}
			<p class="text-warning-400">{$currentPipelineStore.status}</p>
		{:else if $currentPipelineStore.status === 'Completed'}
			<p class="text-success-400">{$currentPipelineStore.status}</p>
		{:else}
			<p>{$currentPipelineStore.status}</p>
		{/if}
	</div>
	<ul
		use:dndzone={{ items: $currentPipelineStore.components, dropTargetStyle: {} }}
		on:consider={(event) => handleDndConsider(event)}
		on:finalize={(event) => handleDndFinalize(event)}
		class="grid gap-4"
	>
		{#each $currentPipelineStore.components as component (component.id)}
			<div animate:flip={{ duration: flipDurationMs }}>
				<PipelineComponent {component} on:deletion={deleteComponent} />
			</div>
		{/each}
	</ul>
</div>
