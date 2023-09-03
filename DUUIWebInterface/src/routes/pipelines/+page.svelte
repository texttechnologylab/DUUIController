<script lang="ts">
	import type { DUUIPipeline } from '$lib/data';
	import { pipelineFilterStore } from '$lib/store';
	import ChipItem from '../../components/ChipItem.svelte';
	import PipelinePreview from '../../components/Pipeline/PipelinePreview.svelte';

	export let data;
	let { pipelines } = data;

	let searchTerm: string = '';
	let filteredPipeliens: DUUIPipeline[];

	$: {
		filteredPipeliens = pipelines.filter(
			(item: DUUIPipeline) =>
				$pipelineFilterStore.length === 0 || $pipelineFilterStore.indexOf(item.status) !== -1
		);

		filteredPipeliens =
			searchTerm === ''
				? filteredPipeliens
				: pipelines.filter((item: DUUIPipeline) => item.name.toLowerCase().indexOf(searchTerm.toLowerCase()) !== -1);
	}
</script>

<svelte:head>
	<title>Pipelines</title>
	<meta name="description" content="All pipelines" />
</svelte:head>

<div class="min-h-screen">
	<div class="max-w-7xl mx-auto flex flex-col items-center justify-center px-4 xl:px-32 py-8 gap-4">
		<input class="border-2 p-2 shadow-md px-6" bind:value={searchTerm} />
		<div class="flex items-center justify-center gap-4">
			<ChipItem text={'Completed'} />
			<ChipItem text={'Error'} />
			<ChipItem text={'Canceled'} />
			<ChipItem text={'New'} />
		</div>
	</div>
	<div class="px-32 py-4 grid grid-cols-1 2xl:grid-cols-4 gap-8">
		{#each filteredPipeliens as pipeline (pipeline.id)}
			<PipelinePreview {pipeline} />
		{/each}
	</div>
</div>
