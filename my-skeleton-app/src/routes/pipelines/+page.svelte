<script lang="ts">
	import { goto } from '$app/navigation'
	import type { DUUIPipeline } from '$lib/data.js'
	import { currentPipelineStore } from '$lib/store.js'
	import { slugify } from '$lib/utils.js'
	import { faEdit, faPlus } from '@fortawesome/free-solid-svg-icons'


	import Fa from 'svelte-fa'

	export let data

	let { pipelines } = data

	const onEditPipeline = (pipeline: DUUIPipeline) => {
		$currentPipelineStore = pipeline
		goto('/pipelines/' + pipeline.id)
	}

	
</script>

<div>
	

	<a href="pipelines/new" class="btn variant-filled-primary">
		<span>Create new</span>
		<Fa icon={faPlus} />
	</a>
</div>

<div class="grid gap-4 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 2xl:grid-cols-5">
	{#each pipelines as pipeline}
		<div class="card p-4 flex items-start variant-outline-primary">
			<div class="grow">
				<p class="h4 font-bold">{pipeline.name}</p>

				{#if pipeline.status === 'Error'}
					<p class="text-error-400">{pipeline.status}</p>
				{:else if pipeline.status === 'Cancelled'}
					<p class="text-warning-400">{pipeline.status}</p>
				{:else if pipeline.status === 'Completed'}
					<p class="text-success-400">{pipeline.status}</p>
				{:else}
					<p>{pipeline.status}</p>
				{/if}
			</div>
			<button class="btn-icon justify-self-end" on:click={() => onEditPipeline(pipeline)}>
				<span>
					<Fa size="lg" icon={faEdit} />
				</span>
			</button>
		</div>
	{/each}
</div>

<!-- <Table source={tableSimple} interactive={true} on:selected={mySelectionHandler} /> -->
