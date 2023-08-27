<script lang="ts">
	import { activePipelineStore } from '../../Interfaces/store';
	import type { DUUIPipelineComponent } from '../../Interfaces/interfaces';
	import PipelineComponent from './PipelineComponent.svelte';
	import { Icon } from 'flowbite-svelte-icons';

	import { dndzone } from 'svelte-dnd-action';
	import { flip } from 'svelte/animate';
	import { onMount } from 'svelte';
	import { v4 as uuidv4 } from 'uuid';

	let pipelineName: string = '';
	let pipelineID: string = '';

	// onMount(() => {
	// 	fetch(`http://127.0.0.1:9090/pipeline?id=${pipelineID}`, {
	// 		method: 'GET',
	// 		mode: 'cors',
	// 	})
	// 		.then((response) => response.text())
	// 		.then((data) => {
	// 			let pipeline = JSON.parse(data);
	// 			pipelineName = pipeline.name
	// 			pipelineID = pipeline.id
	// 			$activePipelineStore = pipeline.components;
	// 		})
	// 		.catch((error) => {
	// 			console.error('Error:', error);
	// 		});
	// })

	// $activePipelineStore = [
	// 	{
	// 		id: 0,
	// 		name: 'LanguageDetection',
	// 		driver: 'DUUIUIMADriver',
	// 		target: 'opennlp.tools.langdetect.LanguageDetector'
	// 	},
	// 	{ id: 1, name: 'spaCy3', driver: 'DUUIRemoteDriver', target: 'http://127.0.0.1:8002' },
	// 	{ id: 2, name: 'GNfinder', driver: 'DUUIDockerDriver', target: 'gnfinder:latest' },
	// 	{ id: 3, name: 'TaxonGazetter', driver: 'DUUIRemoteDriver', target: 'http://127.0.0.1:8001' },
	// 	{
	// 		id: 4,
	// 		name: 'SentimentBERT',
	// 		driver: 'DUUIDockerDriver',
	// 		target: 'docker.bert/sentiment:latest'
	// 	},
	// 	{ id: 5, name: 'SRL', driver: 'DUUISwarmDriver', target: 'srl:1.0' },
	// 	{
	// 		id: 6,
	// 		name: 'XMI-Writer',
	// 		driver: 'DUUIUIMADriver',
	// 		target: 'org.dkpro.core.io.xmi.XmiWriter'
	// 	}
	// ];

	const newComponent = () => {
		var size = $activePipelineStore.length;
		$activePipelineStore[size] = {
			name: 'Component ' + ($activePipelineStore.length + 1),
			driver: 'DUUIDockerDriver',
			id: uuidv4() // This is just temporary so svelte dnd actions works
		};
	};

	async function postPipeline() {
		const data = JSON.stringify({
			id: pipelineID,
			name: pipelineName,
			components: $activePipelineStore
		});

		fetch('http://127.0.0.1:9090/pipeline', {
			method: 'POST',
			mode: 'cors',
			body: data
		})
			.then((response) => response.text())
			.then((xml) => {
				console.log(xml);
			})
			.catch((error) => {
				console.error('Error:', error);
			});
	}

	function handleDndConsider(event: CustomEvent<DndEvent<DUUIPipelineComponent>>) {
		$activePipelineStore = event.detail.items;
		$activePipelineStore = [...$activePipelineStore];
	}

	function handleDndFinalize(event: CustomEvent<DndEvent<DUUIPipelineComponent>>) {
		$activePipelineStore = event.detail.items;
		$activePipelineStore = [...$activePipelineStore];
	}

	let flipDurationMs = 300;
</script>

<input
	bind:value={pipelineName}
	type="text"
	placeholder="Pipeline Name"
	class="text-4xl bg-gray-100 text-black font-bold outline-none p-4 min-w-[32rem] text-center my-8"
/>

<div class="wrapper | grid flex-col gap-8 my-16">
	{#if $activePipelineStore.length === 0 && pipelineName !== ''}
		<h2 class="text-center text-3xl pb-8">Add a Component to your Pipeline</h2>
	{/if}
	{#if pipelineName !== ''}
		{#if $activePipelineStore.length !== 0}
			<ul
				use:dndzone={{ items: $activePipelineStore, dropTargetStyle: {} }}
				on:consider={(event) => handleDndConsider(event)}
				on:finalize={(event) => handleDndFinalize(event)}
				class="flex flex-col gap-4 p-8 py-8 m-auto min-w-[800px] shadow-md rounded-sm"
			>
				{#each $activePipelineStore as component (component.id)}
					<div animate:flip={{ duration: flipDurationMs }}>
						<PipelineComponent {component} />
					</div>
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
	{/if}

	{#if $activePipelineStore.length !== 0}
		<div class="flex flex-col gap-8 items-center p-8">
			<button
				class="border-none bg-slate-800 enabled:hover:bg-slate-600 text-white
					   rounded-full px-8 py-4 flex items-center justify-center gap-4
					   disabled:opacity-50"
				on:click={postPipeline}
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
