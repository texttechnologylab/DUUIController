<script lang="ts">
	import { goto } from '$app/navigation';
	import { blankPipeline } from '$lib/data';
	import { currentPipelineStore } from '$lib/store';

	const setupNewPipeline = () => {
		$currentPipelineStore = blankPipeline();
		goto('/create/new');
	};

</script>

<svelte:head>
	<title>Pipeline Builder</title>
	<meta name="description" content="Build a new Pipeline" />
</svelte:head>

<div class="grid grid-cols-2 text-center gap-4 min-h-full flex-1">
	<button on:click={() => setupNewPipeline()} class="shadow-md p-4">Start from scratch</button>
	<button on:click={() => goto("/")} class="shadow-md p-4">Start with Template</button>
</div>
