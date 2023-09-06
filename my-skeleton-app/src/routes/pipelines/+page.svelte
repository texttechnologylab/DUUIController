<script lang="ts">
	import { goto } from '$app/navigation'
	import type { DUUIPipeline } from '$lib/data.js'
	import { currentPipelineStore } from '$lib/store.js'
	import { slugify } from '$lib/utils.js'
	import {
		faEdit,
		faFilter,
		faPlus,
		faSort,
		faSortAlphaDesc,
		faSortAlphaDownAlt,
		faSortAmountDown,
		faSortAmountDownAlt,
		faSortNumericAsc,
		faSortUp
	} from '@fortawesome/free-solid-svg-icons'
	import { ListBox, ListBoxItem, popup, type PopupSettings } from '@skeletonlabs/skeleton'

	import Fa from 'svelte-fa'

	export let data

	let { pipelines } = data
	let filters: string[] = []

	const filterPopup: PopupSettings = {
		event: 'focus-click',
		target: 'filterPopup',
		placement: 'bottom-start',
		middleware: {
			offset: 8
		}
	}

	let pipelinesFiltered = pipelines

	$: {
		if (filters.length === 0) {
			pipelinesFiltered = pipelines
		} else {
			pipelinesFiltered = pipelines.filter((p) => filters.includes(p.status))
		}
	}
</script>

<div class="card shadow-xl p-4 rounded-md" data-popup="filterPopup">
	<ListBox multiple={true} active="variant-filled-primary" rounded="rounded-md">
		<ListBoxItem bind:group={filters} name="medium" value="New">New</ListBoxItem>
		<ListBoxItem bind:group={filters} name="medium" value="Running">Running</ListBoxItem>
		<ListBoxItem bind:group={filters} name="medium" value="Completed">Completed</ListBoxItem>
		<ListBoxItem bind:group={filters} name="medium" value="Cancelled">Cancelled</ListBoxItem>
		<ListBoxItem bind:group={filters} name="medium" value="Error">Error</ListBoxItem>
	</ListBox>
</div>

<div class="container h-full mx-auto flex flex-col space-y-4 my-16">
	<div class="flex gap-4">
		<a href="pipelines/new" class="btn variant-filled-primary">
			<span>Create</span>
			<Fa icon={faPlus} />
		</a>
		<button class="btn variant-filled-primary flex items-center" use:popup={filterPopup}>
			<span> Filter </span>
			<span><Fa icon={faFilter} /></span>
		</button>
	</div>

	<div class="grid gap-4 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
		{#each pipelinesFiltered as pipeline}
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
				<a class="btn-icon justify-self-end" href="/pipelines/{pipeline.id}">
					<span>
						<Fa size="lg" icon={faEdit} />
					</span>
				</a>
			</div>
		{/each}
	</div>
</div>

<!-- <Table source={tableSimple} interactive={true} on:selected={mySelectionHandler} /> -->
