<script lang="ts">
	import { datetimeToString, includes } from '$lib/utils/text'
	import {
		faCheck,
		faFileExport,
		faFileImport,
		faPlus,
		faSearch,
		faWifi,
		faX
	} from '@fortawesome/free-solid-svg-icons'

	import Fa from 'svelte-fa'

	export let data

	let { pipelines } = data

	// pipelines.forEach(({ components }) => {
	// 	components.forEach((component: DUUIComponent) => {
	// 		$componentsStore = [...$componentsStore, component]
	// 	})
	// })

	let searchText: string = ''
	let filteredPipelines = pipelines

	let activeOnly: boolean = false
	let isNew: boolean = true

	$: {
		filteredPipelines = pipelines.filter(
			(pipeline) => includes(pipeline.name, searchText) || !searchText
		)

		if (activeOnly) {
			filteredPipelines = filteredPipelines.filter((pipeline) => pipeline.serviceStartTime !== 0)
		}

		filteredPipelines = filteredPipelines.filter((pipeline) => pipeline.timesUsed === 0 && isNew)
	}
</script>

<svelte:head>
	<title>Pipelines</title>
</svelte:head>
<div class="container h-full mx-auto flex flex-col space-y-4 md:my-8">
	<h1 class="h2">Pipelines</h1>
	<hr />

	<div class="flex items-center justify-between gap-4">
		<a href="pipelines/new" class="btn variant-soft-surface">
			<span>Create</span>
			<Fa icon={faPlus} />
		</a>
		<button class="btn rounded-md variant-soft-surface">
			<Fa icon={faFileImport} />
			<span>Import</span>
			<!-- TODO: Dialog / Modal -->
		</button>

		<div class="flex items-center gap-4 ml-auto">
			<input
				class="input variant-soft-surface"
				type="text"
				placeholder="Search..."
				bind:value={searchText}
			/>
			<button class="chip rounded-md variant-soft-surface" on:click={() => (isNew = !isNew)}>
				<Fa icon={isNew ? faCheck : faX} />
				<span>New</span>
			</button>
			<button
				class="chip rounded-md variant-soft-surface"
				on:click={() => (activeOnly = !activeOnly)}
			>
				<Fa icon={activeOnly ? faCheck : faX} />
				<span>Active</span>
			</button>
		</div>
	</div>

	<div class="grid gap-4 md:gap-8 md:grid-cols-2 lg:grid-cols-3 relative">
		{#each filteredPipelines as pipeline}
			<a
				class="card grid gap-4 rounded-md shadow-lg p-4 items-start"
				href="/pipelines/{pipeline.id}"
			>
				<div class="flex items-center justify-between">
					<p class="h4 break-words">{pipeline.name}</p>
					{#if pipeline.serviceStartTime !== 0}
						<Fa icon={faWifi} class="text-success-400" />
					{/if}
				</div>
				<div class="flex items-center justify-between">
					<p>{pipeline.components.length} Component(s)</p>
					{#if pipeline.lastUsed}
						<p>Last used: {datetimeToString(new Date(pipeline.lastUsed))}</p>
					{:else}
						<p>Last used: Never</p>
					{/if}
				</div>
			</a>
		{/each}
	</div>
</div>

<!-- <Table source={tableSimple} interactive={true} on:selected={mySelectionHandler} /> -->
<style>
</style>
