<script lang="ts">
	import PipelineComponent from './PipelineComponent.svelte';
	import { Icon } from 'flowbite-svelte-icons';

	import { dndzone, type DndEvent } from 'svelte-dnd-action';
	import { flip } from 'svelte/animate';
	import { v4 as uuidv4 } from 'uuid';
	import { updatePipeline } from '../../requests/update';
	import { beforeNavigate } from '$app/navigation';
	import { currentPipelineStore } from '$lib/store';
	import { blankComponent, type DUUIPipelineComponent } from '$lib/data';

	function handleDndConsider(event: CustomEvent<DndEvent<DUUIPipelineComponent>>) {
		$currentPipelineStore.components = event.detail.items;
		$currentPipelineStore.components = [...$currentPipelineStore.components];
	}

	function handleDndFinalize(event: CustomEvent<DndEvent<DUUIPipelineComponent>>) {
		$currentPipelineStore.components = event.detail.items;
		$currentPipelineStore.components = [...$currentPipelineStore.components];
	}

	let flipDurationMs = 300;

	$currentPipelineStore.components.forEach((component) => {
		if (component.id === undefined) {
			component.id = uuidv4();
		}
	});

	const addComponent = () => {
		var size = $currentPipelineStore.components.length;
		$currentPipelineStore.components = [
			...$currentPipelineStore.components,
			blankComponent($currentPipelineStore.id, 'Component ' + (size + 1))
		];

		hasChanged = true;
	};

	function deleteComponent(event: { detail: { id: any } }) {
		$currentPipelineStore.components = $currentPipelineStore.components.filter(function (
			value,
			i,
			arr
		) {
			if (value.id !== event.detail.id) return value;
		});
		hasChanged = true;
	}

	async function saveChanges() {
		await updatePipeline($currentPipelineStore);
		hasChanged = false;
	}

	let hasChanged: boolean = false; // document has changes to save

	beforeNavigate(({ from, to, cancel }) => {
		if (hasChanged && !confirm('Leave without saving ?')) {
			cancel();
		}
	});
</script>

{#if $currentPipelineStore !== null}
	<div class="shadow-md bg-[#f5f5f5] mx-auto grow" >
		<!-- <header class="flex items-center justify-between bg-blue-500 text-white">
			<input
				bind:value={$currentPipelineStore.name}
				type="text"
				placeholder="Pipeline Name"
				class="p-4 border-r-2 grow bg-blue-500 focus-within:bg-blue-600 text-xl"
			/>
			<button
				class="enabled:hover:bg-blue-600 text-white
					   p-4 flex items-center justify-center gap-4
					   disabled:opacity-50"
				on:click={saveChanges}
			>
				<p>Save Changes</p>
				<Icon name="file-check-solid" class="w-6 h-6" />
			</button>
		</header> -->

		<ul
			use:dndzone={{ items: $currentPipelineStore.components, dropTargetStyle: {} }}
			on:consider={(event) => handleDndConsider(event)}
			on:finalize={(event) => handleDndFinalize(event)}
			class="flex flex-col items-center justify-start gap-4 p-8"
		>
			{#each $currentPipelineStore.components as component (component.id)}
				<div animate:flip={{ duration: flipDurationMs }}>
					<PipelineComponent {component} on:deletion={deleteComponent} />
				</div>
			{/each}
		</ul>
		<div class="flex items-center justify-center mb-8">
			<button
				class="border-none bg-blue-500 hover:bg-blue-600 text-white rounded-full p-4"
				on:click={addComponent}
			>
				<Icon name="plus-solid" class="w-6 h-6" />
			</button>
		</div>
		<!-- {#if $currentPipelineStore.components.length === 0 && $currentPipelineStore.name !== ''}
		<h2 class="text-center text-3xl pb-8">Add a Component to your Pipeline</h2>
	{/if}
	{#if $currentPipelineStore.name !== ''}
		{#if $currentPipelineStore.components.length !== 0}
			<ul
				use:dndzone={{ items: $currentPipelineStore.components, dropTargetStyle: {} }}
				on:consider={(event) => handleDndConsider(event)}
				on:finalize={(event) => handleDndFinalize(event)}
				class="flex flex-col gap-4 p-8 py-8 m-auto min-w-[800px] shadow-md rounded-sm"
			>
				{#each $currentPipelineStore.components as component (component.id)}
					<div animate:flip={{ duration: flipDurationMs }}>
						<PipelineComponent {component} />
					</div>
				{/each}
			</ul>
		{/if}
	
	{/if}

	{#if $currentPipelineStore.components.length !== 0}
		
	{/if} -->
	</div>
{/if}
