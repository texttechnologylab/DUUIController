<script lang="ts">
	import { page } from '$app/stores'
	import DriverIcon from '$lib/svelte/DriverIcon.svelte'
	import { usedDrivers, type DUUIPipeline } from '$lib/duui/pipeline'
	import { faClone } from '@fortawesome/free-solid-svg-icons'
	import Fa from 'svelte-fa'
	import { createEventDispatcher } from 'svelte'

	export let pipeline: DUUIPipeline
	const editor: boolean = $page.url.pathname === '/pipelines'
	const dispatcher = createEventDispatcher()

	const onClone = () => {
		dispatcher('clone', pipeline)
	}
</script>

<div class="flex items-center gap-4 justify-between">
	<h4 class="h4 font-bold">{pipeline.name}</h4>
	{#if pipeline.user_id === null || pipeline.user_id === undefined}
		<button on:click={onClone} class="btn variant-soft gap-4 animate-text"
			><Fa icon={faClone} size="lg" /> Clone template</button
		>
	{:else}
		<p class="badge variant-soft-primary {editor ? 'hidden' : ''}">User</p>
	{/if}
</div>

<p class="">{pipeline.description}</p>

<div class="flex flex-wrap gap-2">
	{#each pipeline.tags as tag}
		<span class="chip variant-glass-primary">
			{tag}
		</span>
	{/each}
</div>
<div class="pt-4 flex items-center justify-between self-end">
	<p>{pipeline.components.length} Component{pipeline.components.length > 1 ? 's' : ''}</p>
	<p class="hidden md:block">{pipeline.times_used}</p>
	<div class="flex items-center gap-4">
		{#each usedDrivers(pipeline) as driver}
			<DriverIcon {driver} />
		{/each}
	</div>
</div>
