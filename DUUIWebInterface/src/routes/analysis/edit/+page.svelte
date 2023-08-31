<script lang="ts">
	import { activePipelineStore, editedPipeline, pipelineResults } from '../../../Interfaces/store';
	import { onMount } from 'svelte';

	import Pipeline from '../../../components/Pipeline/Pipeline.svelte';
	import { Icon } from 'flowbite-svelte-icons';

	onMount(() => {
		if ($editedPipeline !== null) {
			$activePipelineStore = $editedPipeline.components;
		}
	});

	const runPipeline = () => {
		$pipelineResults.set($editedPipeline.id, 'Running');

		fetch('http://127.0.0.1:9090/run/' + $editedPipeline.id, {
			method: 'GET',
			mode: 'cors'
		})
			.then((response) => response.text())
			.then((xml) => {
				console.log(xml);
				$pipelineResults.set($editedPipeline.id, 'Success');
			})
			.catch((error) => {
				$pipelineResults.set($editedPipeline.id, 'Error');
			});
	};

	let result: string = '';
	
</script>

{#if editedPipeline !== null}
	<div class="flex items-center flex-col">
		<Pipeline
			pipelineName={$editedPipeline.name}
			pipelineID={$editedPipeline.id}
			inEditMode={true}
		/>
		<button
			class="border-none bg-slate-800 enabled:hover:bg-slate-600 text-white
					   rounded-full px-8 py-4 flex items-center justify-center gap-4
					   disabled:opacity-50"
			on:click={runPipeline}
		>
			<p>Run Pipeline</p>
			<Icon name="rocket-solid" class="w-6 h-6" />
		</button>
	</div>
	{#if result}
		{result}
	{/if}
{/if}
