<!--
	@component
	This is a wrapper for the display of pipeline's in a compact format.render
	Outside of the Builder page a badge is displayed in the top right corner to indicate
	whether the pipeline is a template or user owned.
 -->

<script lang="ts">
	import { page } from '$app/stores'
	import { usedDrivers, type DUUIPipeline } from '$lib/duui/pipeline'
	import DriverIcon from '$lib/svelte/components/DriverIcon.svelte'

	export let pipeline: DUUIPipeline
	const builder: boolean = $page.url.pathname === '/pipelines'
</script>

<div class="flex-center-4 justify-between">
	<h4 class="h4">{pipeline.name}</h4>

	<!-- Is user owned or template -->
	{#if pipeline.user_id === null || pipeline.user_id === undefined}
		<p class="badge variant-soft-tertiary">Template</p>
	{:else}
		<p class="badge variant-soft-primary {builder ? 'hidden' : ''}">User</p>
	{/if}
</div>

<!-- Description -->
<p class="line-clamp-2">{pipeline.description}</p>

<div class="self-end space-y-8">
	<div class="pt-4 flex items-center justify-between self-end">
		<!-- # of components -->
		<p>{pipeline.components.length} Component{pipeline.components.length !== 1 ? 's' : ''}</p>

		<!-- Times used -->
		<p>{pipeline.times_used}</p>

		<!-- Drivers -->
		<div class="flex-center-4">
			{#each usedDrivers(pipeline) as driver}
				<DriverIcon {driver} />
			{/each}
		</div>
	</div>

	<!-- Tags -->
	<div class="flex flex-wrap gap-2 self-end">
		{#each pipeline.tags.sort((a, b) => (a < b ? -1 : 1)) as tag}
			<span class="tag">
				{tag}
			</span>
		{/each}
	</div>
</div>
