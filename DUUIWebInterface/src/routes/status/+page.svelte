<script lang="ts">
	import {
		DUUIUIMADriver,
		type DUUIPipeline,
		DUUIRemoteDriver,
		DUUIDockerDriver,
		DUUISwarmDriver,
		type DUUIPipelineComponent
	} from '../../Interfaces/interfaces';

	let pipeline: DUUIPipeline = {
		name: 'Example',
		components: [
			{
				displayName: 'LanguageDetection',
				driver: new DUUIUIMADriver(),
				id: null
			},
			{
				displayName: 'spaCy3',
				driver: new DUUIRemoteDriver(),
				id: null
			},
			{
				displayName: 'GNfinder',
				driver: new DUUIDockerDriver(),
				id: null
			},
			{
				displayName: 'TaxonGazetter',
				driver: new DUUIRemoteDriver(),
				id: null
			},
			{
				displayName: 'SentimentBart',
				driver: new DUUIDockerDriver(),
				id: null
			},
			{
				displayName: 'SRL',
				driver: new DUUISwarmDriver(),
				id: null
			},
			{
				displayName: 'XMI-Writer',
				driver: new DUUIUIMADriver(),
				id: null
			}
		]
	};
	function drop(event: DragEvent) {
		event.preventDefault();
		const [item] = pipeline.components.splice(dragIndex, 1);
		pipeline.components.splice(dropIndex, 0, item);
		pipeline.components = pipeline.components;

		dragIndex = -1;
		dropIndex = -1;
	}

	let dragIndex: number;
	let dropIndex: number;
</script>

<p>Drag & Drop Components</p>
<p class="text-2xl my-4">{pipeline.name}</p>

<ul on:drop={(event) => drop(event)} ondragover="return false" class="flex flex-col gap-4 mt-4">
	{#each pipeline.components as component, index}
		<li
			class:hovering={index === dropIndex}
			class="p-4 border-[1px] border-slate-200 hover:bg-orange-400 hover:text-white"
			draggable={true}
			on:dragenter={() => (dropIndex = index)}
			on:dragstart={() => (dragIndex = index)}
		>
			{component.displayName}
		</li>
	{/each}
</ul>

<style>
	.hovering {
		border: 1px solid orange;
	}
</style>
