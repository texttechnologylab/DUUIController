<script lang="ts">
	export let component: DUUIPipelineComponent;
	export let index: number;
</script>

<div class="component">
	<div class="header">
		<p class="pipeline__component-title">{component.displayName}</p>
		<select class="text-md px-4 py-2 bg-slate-800 text-white">
			<option value="docker">Docker</option>
			<option value="swarm">Swarm</option>
			<option value="remote">Remote</option>
			<option value="uima">UIMA</option>
		</select>
	</div>
	{#if component.id !== null}
		<p class="pipeline__component-id">{component.id}</p>
	{/if}
	<p class="pipeline__component-driver">{component.driver.displayName}</p>
	<p class="pipeline__component-driver">{component.description}</p>
</div>

<style>
	.component {
		padding: 1rem 2rem;
		box-shadow: 0px 4px 12px 0px rgba(0, 0, 0, 0.25);
		display: flex;
		flex-direction: column;
		align-items: flex-start;
		gap: 1rem;
		border-radius: 0.25rem;
	}
	.header {
		display: flex;
		gap: 1rem;
	}

	.pipeline__component-status {
		margin-left: auto;
	}
</style>
