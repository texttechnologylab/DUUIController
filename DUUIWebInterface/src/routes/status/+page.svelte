<script lang="ts">
	import { pipelines, pipelineResults } from '../../Interfaces/store';
	import type { DUUIPipeline } from '../../Interfaces/interfaces';
	import { onMount } from 'svelte';

	onMount(() => {
		async function getPipelineStatus() {
			$pipelines.forEach((pipeline) => {
				fetch('http://127.0.0.1:9090/status/' + pipeline.id, {
					method: 'GET',
					mode: 'cors'
				})
					.then((response) => response.text())
					.then((status) => {
						$pipelineResults.set(pipeline.id, status);
						$pipelineResults = $pipelineResults;
					})
					.catch((error) => {
						$pipelineResults.set(pipeline.id, 'Error');
						$pipelineResults = $pipelineResults;
					});
			});
		}
		const interval = setInterval(getPipelineStatus, 5000);

		getPipelineStatus();

		return () => clearInterval(interval);
	});

	const runPipeline = (pipeline: DUUIPipeline) => {
		$pipelineResults.set(pipeline.id, 'Running');
		$pipelineResults = $pipelineResults;

		fetch('http://127.0.0.1:9090/run/' + pipeline.id, {
			method: 'GET',
			mode: 'cors'
		})
			.then((response) => response.text())
			.then((xml) => {
				if (xml == 'Cancelled') {
					$pipelineResults.set(pipeline.id, 'Cancelled');
				} else {
					$pipelineResults.set(pipeline.id, 'Running');
				}
				$pipelineResults = $pipelineResults;
				result = xml;
			})
			.catch((error) => {
				$pipelineResults.set(pipeline.id, 'Error');
				$pipelineResults = $pipelineResults;
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
				$pipelineResults.set(pipeline.id, 'Cancelled');
				$pipelineResults = $pipelineResults;
				console.log($pipelineResults);

				result = xml;
			})
			.catch((error) => {
				$pipelineResults.set(pipeline.id, 'Error');
				$pipelineResults = $pipelineResults;
			});
	};

	let result: string = '';

	const handleClick = (pipeline: DUUIPipeline) => {
		switch ($pipelineResults.get(pipeline.id)) {
			case 'Idle':
				runPipeline(pipeline);
				break;
			case 'Success':
				console.log(result);
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
