<script lang="ts">
	import ActionButton from '$lib/components/ActionButton.svelte'
	import DriverIcon from '$lib/components/DriverIcon.svelte'
	import { usedDrivers } from '$lib/duui/pipeline'
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
	let unused: boolean = false

	$: {
		filteredPipelines = pipelines.filter(
			(pipeline) => includes(pipeline.name, searchText) || !searchText
		)

		if (activeOnly) {
			filteredPipelines = filteredPipelines.filter((pipeline) => pipeline.serviceStartTime !== 0)
		}

		filteredPipelines = filteredPipelines.filter((pipeline) => pipeline.timesUsed === 0 || !unused)
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
		<ActionButton
			text="Import"
			icon={faFileImport}
			on:click={() => console.log('IMPORT NOT IMPLEMENTED')}
		/>
		<div class="flex items-center gap-4 ml-auto">
			<input
				class="input variant-soft-surface"
				type="text"
				placeholder="Search..."
				bind:value={searchText}
			/>
			<button class="chip rounded-md variant-soft-surface" on:click={() => (unused = !unused)}>
				<Fa icon={unused ? faCheck : faX} />
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
				class="text-left variant-soft-surface p-4 hover:shadow-lg space-y-4 flex flex-col"
				href="/pipelines/{pipeline.oid}"
			>
				<div class="flex items-center gap-4 justify-between">
					<p class="text-lg font-bold">{pipeline.name}</p>
					{#if pipeline.serviceStartTime !== 0}
						<Fa icon={faWifi} class="text-success-400" />
					{/if}
				</div>

				<p class="grow">{pipeline.description}</p>
				<div class="pt-4 flex items-center justify-between">
					<p>{pipeline.components.length} Component(s)</p>
					<div class="flex items-center gap-4">
						{#each usedDrivers(pipeline) as driver}
							<DriverIcon {driver} />
						{/each}
					</div>
				</div>
			</a>
		{/each}
	</div>
</div>

<!-- <Table source={tableSimple} interactive={true} on:selected={mySelectionHandler} /> -->
<style>
</style>
