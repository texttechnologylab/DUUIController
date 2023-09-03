<script lang="ts">
	import { pipelines, editedPipeline } from '../../../Interfaces/store';
	import type { DUUIPipelineComponent } from '../../../Interfaces/interfaces';
	import { Icon } from 'flowbite-svelte-icons';

	import { dndzone, type DndEvent } from 'svelte-dnd-action';
	import { flip } from 'svelte/animate';
	import { v4 as uuidv4 } from 'uuid';
	import { beforeNavigate, goto } from '$app/navigation';
	import PipelineComponent from '../../../components/Pipeline/PipelineComponent.svelte';
	import { insertPipeline } from '../../../requests/post';
	import { getPipelines } from '../../../requests/get';

	function handleDndConsider(event: CustomEvent<DndEvent<DUUIPipelineComponent>>) {
		$editedPipeline.components = event.detail.items;
		$editedPipeline.components = [...$editedPipeline.components];
	}

	function handleDndFinalize(event: CustomEvent<DndEvent<DUUIPipelineComponent>>) {
		$editedPipeline.components = event.detail.items;
		$editedPipeline.components = [...$editedPipeline.components];
	}

	let flipDurationMs = 300;

	$editedPipeline.components.forEach((component) => {
		if (component.id === undefined) {
			component.id = uuidv4();
		}
	});

	const addComponent = () => {
		var size = $editedPipeline.components.length;
		$editedPipeline.components[size] = {
			name: 'Component ' + ($editedPipeline.components.length + 1),
			driver: 'DUUIDockerDriver',
			target: '',
			_id: '',
			id: uuidv4()
		};
		$editedPipeline.components = $editedPipeline.components;
		hasChanged = true;
	};

	function deleteComponent(event: { detail: { id: any } }) {
		$editedPipeline.components = $editedPipeline.components.filter(function (value, i, arr) {
			if (value.id !== event.detail.id) return value;
		});
		hasChanged = true;
	}

	async function finalizePipeline() {
		console.log(await insertPipeline($editedPipeline));
		hasChanged = false;
		$pipelines = await getPipelines();
		goto('/pipelines');
	}

	let hasChanged: boolean = false; // document has changes to save

	beforeNavigate(({ from, to, cancel }) => {
		if (hasChanged && !confirm('Leave without saving ?')) {
			cancel();
		}
	});
</script>

{#if $editedPipeline !== null}
	<div class="flex flex-col shadow-lg border-2 border-blue-500 rounded-md overflow-hidden">
		<header class="flex items-center justify-between bg-blue-500 text-white">
			<input
				bind:value={$editedPipeline.name}
				type="text"
				placeholder="Pipeline Name"
				class="p-4 border-r-2 grow bg-blue-500 focus-within:bg-blue-600 text-xl"
			/>
			<button
				class="enabled:hover:bg-blue-600 text-white
					   p-4 flex items-center justify-center gap-4
					   disabled:opacity-50"
				on:click={finalizePipeline}
			>
				<p>Finalize</p>
				<Icon name="file-check-solid" class="w-6 h-6" />
			</button>
		</header>

		<ul
			use:dndzone={{ items: $editedPipeline.components, dropTargetStyle: {} }}
			on:consider={(event) => handleDndConsider(event)}
			on:finalize={(event) => handleDndFinalize(event)}
			class="flex flex-col gap-4 p-8 py-8 m-auto min-w-[800px]"
		>
			{#each $editedPipeline.components as component (component.id)}
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
	</div>
{/if}
