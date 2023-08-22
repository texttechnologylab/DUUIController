<script lang="ts">
	import PipelineComponent from './PipelineComponent.svelte';

	export let components: DUUIPipelineComponent[] = [
		{
			displayName: "BreakIteratorSegmenter",
			driver: new DUUIUIMADriver(),
			id: null,
			description: ""
		}
	];
</script>

<div class="wrapper | flex flex-col">
	{#each components as component, index}
		<PipelineComponent {component} {index} />
	{/each}
</div>
