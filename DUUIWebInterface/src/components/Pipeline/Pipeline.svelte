<script lang="ts">
	import type { DUUIPipeline } from '../../Interfaces/interfaces';
	import DuuiRemoteComponent from './DUUIRemoteComponent.svelte';
	import PipelineComponent from './PipelineComponent.svelte';
	import { Icon } from 'flowbite-svelte-icons';

	let pipeline: DUUIPipeline = {
		name: '',
		components: [
			{
				displayName: 'LanguageDetection',
				driver: 'DUUIUIMADriver',
				id: null
			},
			{
				displayName: 'spaCy3',
				driver: 'DUUIRemoteDriver',
				id: null
			},
			{
				displayName: 'GNfinder',
				driver: 'DUUIDockerDriver',
				id: null
			},
			{
				displayName: 'TaxonGazetter',
				driver: 'DUUIRemoteDriver',
				id: null
			},
			{
				displayName: 'SentimentBart',
				driver: 'DUUIDockerDriver',
				id: null
			},
			{
				displayName: 'SRL',
				driver: 'DUUISwarmDriver',
				id: null
			},
			{
				displayName: 'XMI-Writer',
				driver: 'DUUIUIMADriver',
				id: null
			}
		]
	};

	const newComponent = () => {
		pipeline.components = [
			...pipeline.components,
			{
				displayName: 'Component ' + (pipeline.components.length + 1),
				driver: 'DUUIDockerDriver',
				id: null
			}
		];
		pipeline = pipeline;
	};

	async function build() {
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

<!-- <input
	bind:value={pipeline.name}
	type="text"
	placeholder="Pipeline Name"
	class="text-4xl border-b-[1px] border-b-slate-800 font-bold outline-none p-4 min-w-[32rem] text-center my-8"
/> -->

<div class="wrapper | grid flex-col gap-8 my-16">
	{#if pipeline.components.length === 0}
		<h2 class="text-center text-3xl pb-8">Add a Component to your Pipeline</h2>
	{:else}
		<ul
			on:drop={(event) => drop(event)}
			ondragover="return false"
			class="flex flex-col gap-12 p-8 py-8 m-auto min-w-[800px] shadow-md rounded-sm"
		>
			{#each pipeline.components as component, index}
				<PipelineComponent
					on:dragenter={() => (dropIndex = index)}
					on:dragstart={() => (dragIndex = index)}
					{component}
					expanded={false}
					on:delete={deleteComponent}
				>
					{#if component.driver === 'DUUIRemoteDriver'}
						<DuuiRemoteComponent />
					{/if}
				</PipelineComponent>
			{/each}
		</ul>
	{/if}

	<div class="flex items-center justify-center m-8">
		<button
			class="border-none bg-slate-800 hover:bg-slate-600 text-white rounded-full p-4"
			on:click={newComponent}
		>
			<Icon name="plus-solid" class="w-6 h-6" />
		</button>
	</div>
	{#if pipeline.components.length !== 0}
		<div class="flex flex-col gap-8 items-center p-8">
			<button
				class="border-none bg-slate-800 enabled:hover:bg-slate-600 text-white
					   rounded-full px-8 py-4 flex items-center justify-center gap-4
					   disabled:opacity-50"
				on:click={build}
			>
				<p>Finalize Pipeline</p>
				<Icon name="upload-outline" class="w-6 h-6" />
			</button>

			<!-- <label for="savePipeline" class="flex gap-4 items-center justify-center">
				<input
					type="checkbox"
					id="savePipeline"
					class="checked:accent-slate-800 w-5 h-5"
					bind:checked={save}
				/>
				Save Pipeline as Template?
			</label> -->
		</div>
	{/if}
</div>
