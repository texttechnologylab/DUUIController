<script lang="ts">
	import { pipelines, pipelineResults } from '../../Interfaces/store';
	import type { DUUIPipeline } from '../../Interfaces/interfaces';

	const runPipeline = (pipeline: DUUIPipeline) => {
		$pipelineResults.set(pipeline.id, 'Running');
		$pipelineResults = $pipelineResults;

		fetch('http://127.0.0.1:9090/run/' + pipeline.id, {
			method: 'GET',
			mode: 'cors'
		})
			.then((response) => response.text())
			.then((xml) => {
				$pipelineResults.set(pipeline.id, 'Success');
				$pipelineResults = $pipelineResults
				result = xml;
			})
			.catch((error) => {
				$pipelineResults.set(pipeline.id, 'Error');
				$pipelineResults = $pipelineResults
			});
	};

	const cancelPipeline = (pipeline: DUUIPipeline) => {
		$pipelineResults.set(pipeline.id, 'Canceled');
		$pipelineResults = $pipelineResults;

		fetch('http://127.0.0.1:9090/cancel/' + pipeline.id, {
			method: 'POST',
			mode: 'cors'
		})
			.then((response) => response.text())
			.then((xml) => {
				$pipelineResults.set(pipeline.id, 'Canceled');
				$pipelineResults = $pipelineResults
				result = xml;
				console.log(xml);
				
			})
			.catch((error) => {
				$pipelineResults.set(pipeline.id, 'Error');
				$pipelineResults = $pipelineResults
			});
	};
	

	let result: string = '';

	const handleClick = (pipeline: DUUIPipeline) => {
		switch ($pipelineResults.get(pipeline.id)) {
			case 'Idle':
				runPipeline(pipeline);
				break;
			case 'Success':
				break;
			case 'Running':
				cancelPipeline(pipeline);

			default:
				break;
		}
	};
</script>

<div class="grid grid-cols-4 gap-4">
	{#each $pipelines as pipeline}
		<div class="flex flex-col items-center justify-between gap-4 p-8 shadow-md">
			<p>{pipeline.name}</p>
			<p>{$pipelineResults.get(pipeline.id)}</p>
			<button
				on:click={() => handleClick(pipeline)}
				class=" bg-slate-800 text-white rounded-md shadow-md px-8 py-2 hover:bg-slate-700"
			>
				{#if $pipelineResults.get(pipeline.id) === 'Success'}
					Show Result
				{:else if $pipelineResults.get(pipeline.id) === 'Running'}
					Cancel
				{:else if $pipelineResults.get(pipeline.id) === 'Idle'}
					Run
				{:else}
					Restart
				{/if}
			</button>
		</div>
	{/each}
</div>
