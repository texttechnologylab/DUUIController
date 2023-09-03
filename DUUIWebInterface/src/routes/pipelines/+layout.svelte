<!-- <script lang="ts">
	import { onMount } from 'svelte';
	import { getPipelines } from '../../requests/get';

    let pipelines: any

    

	async function loadPipelines() {
		const response = await getPipelines();
		pipelines = await response.json();
		return pipelines;
	}

    $: pipelines = pipelines;


</script>

<div class="pipeline__list my-16 gap-8">
	<aside class="flex flex-col gap-4 border-2 border-blue-500 p-8">
		<h2>Pipelines</h2>
		{#await loadPipelines()}
			<p>Loading Pipelines</p>
		{:then pipelines}
			{#each pipelines as pipeline}
				<a href="/pipelines/{pipeline._id['$oid']}" class="bg-slate-200 p-4"
					>{pipeline._id['$oid']}</a
				>
			{/each}
		{:catch error}
			<p>{error.message}</p>
		{/await}
	</aside>

	<main>
		<slot />
	</main>
</div>

<style>
	.pipeline__list {
		display: grid;
		grid-template-columns: min-content 1fr;
	}
</style> -->
<slot />
