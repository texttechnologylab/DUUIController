<script lang="ts">
	import PipelineComponent from './PipelineComponent.svelte';
	import {
		DUUIDockerDriver,
		DUUISwarmDriver,
		type DUUIPipeline,
		DUUIUIMADriver,
		DUUIRemoteDriver,
		type DUUIPipelineComponent
	} from '../../Interfaces/interfaces';
	import { Icon } from 'flowbite-svelte-icons';

	export let pipeline: DUUIPipeline = {
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

	const newComponent = () => {
		pipeline.components = [
			...pipeline.components,
			{
				displayName: 'Component ' + (pipeline.components.length + 1),
				driver: new DUUIDockerDriver(),
				id: null
			}
		];
	};

	async function build() {
		console.log(pipeline);

		return;

		fetch('http://127.0.0.1:9090/build', {
			method: 'POST',
			mode: 'cors'
			// body: JSON.stringify(components)
		})
			.then((response) => response.text())
			.then((xml) => {
				console.log(xml);
			})
			.catch((error) => {
				console.error('Error:', error);
			});
	}

	let save: boolean = true;
	let pipelineName: string = '';

	function deleteComponent(event: { detail: { item: any } }) {
		pipeline.components = pipeline.components.filter((item, i, arr) => item !== event.detail.item);
	}

	let dragIndex: number;
	let dropIndex: number;

	function drop(event: DragEvent) {
		event.preventDefault();
		const [item] = pipeline.components.splice(dragIndex, 1);
		pipeline.components.splice(dropIndex, 0, item);
		pipeline.components = pipeline.components;

		dragIndex = -1;
		dropIndex = -1;
	}
</script>

<div class="wrapper | flex flex-col gap-8 my-16 max-w-lg m-auto">
	{#if pipeline.components.length === 0}
		<h2 class="text-center text-3xl pb-8">Add a Component to your Pipeline</h2>
	{/if}

	<ul on:drop={(event) => drop(event)} ondragover="return false" class="flex flex-col gap-4 mt-4">
		{#each pipeline.components as component, index}
			<PipelineComponent
				
				on:dragenter={() => (dropIndex = index)}
				on:dragstart={() => (dragIndex = index)}
				{component}
				expanded={false}
				on:delete={deleteComponent}
			>
				<p>Slot</p>
			</PipelineComponent>
		{/each}
	</ul>

	<div class="flex items-center justify-center m-8">
		<button
			class="border-none bg-slate-800 hover:bg-slate-600 text-white rounded-full p-4"
			on:click={newComponent}
		>
			<Icon name="plus-solid" class="w-6 h-6" />
		</button>
	</div>
	{#if pipeline.components.length !== 0}
		<div class="flex flex-col gap-6 shadow-2xl p-8">
			<input
				bind:value={pipeline.name}
				type="text"
				placeholder="Pipeline Name"
				class="text-xl border-slate-800 border-[1px] cursor-pointer outline-none p-4"
			/>
			<button
				disabled={pipelineName === ''}
				class="border-none bg-slate-800 enabled:hover:bg-slate-600 text-white
					   rounded-full px-8 py-4 flex items-center justify-center gap-4
					   disabled:opacity-50"
				on:click={build}
			>
				<p>Build Pipeline</p>
				<Icon name="upload-solid" class="w-6 h-6" />
			</button>

			<label for="savePipeline" class="flex gap-4 items-center justify-center">
				<input
					type="checkbox"
					id="savePipeline"
					class="checked:accent-slate-800 w-5 h-5"
					bind:checked={save}
				/>
				Save Pipeline as Template?
			</label>
		</div>
	{/if}
</div>
