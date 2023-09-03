<script lang="ts">
	import { page } from '$app/stores';
	import type { DUUIPipeline } from '../../../Interfaces/interfaces';
	import PipelinePreview from '../../../components/Pipeline/PipelinePreview.svelte';
	import { getPipeline } from '../../../requests/get';

	let id = $page.params.id;
	let pipeline: DUUIPipeline;

	async function loadPipeline() {
		const response = await getPipeline(id);
		const newPipeline = await response.json();
		pipeline = newPipeline;
		return pipeline;
	}
	$: {
		id = $page.params.id;
		loadPipeline(); // Call your data loading function here
	}
</script>

{#await loadPipeline()}
	<p>Loading Pipeline</p>
{:then newPipeline}
	<PipelinePreview {pipeline}/>
{:catch error}
	<p>{error.message}</p>
{/await}
