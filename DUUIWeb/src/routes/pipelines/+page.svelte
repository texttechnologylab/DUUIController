<script lang="ts">
	import ActionButton from '$lib/svelte/widgets/action/ActionButton.svelte'
	import DriverIcon from '$lib/components/DriverIcon.svelte'
	import { usedDrivers } from '$lib/duui/pipeline'
	import Anchor from '$lib/svelte/widgets/action/Anchor.svelte'
	import { includes } from '$lib/utils/text'
	import { faFileImport, faPlus, faSearch, faWifi } from '@fortawesome/free-solid-svg-icons'

	import Fa from 'svelte-fa'
	import TextInput from '$lib/svelte/widgets/input/TextInput.svelte'

	export let data

	let { pipelines } = data

	let searchText: string = ''
	let filteredPipelines = pipelines

	let activeOnly: boolean = false
	let unused: boolean = false

	$: {
		filteredPipelines = pipelines.filter(
			(pipeline) => includes(pipeline.name + ' ' + pipeline.description, searchText) || !searchText
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
	<hr class="bg-surface-400/20 h-[1px] !border-0 rounded " />

	<div class="grid md:flex items-center md:justify-between gap-4">
		<div class="flex items-center gap-4">
			<Anchor href="/pipelines/new" icon={faPlus} text="Create" />
			<ActionButton
				text="Import"
				icon={faFileImport}
				on:click={() => console.log('IMPORT NOT IMPLEMENTED')}
			/>
		</div>

		<div class="md:ml-auto">
			<TextInput bind:value={searchText} icon={faSearch} placeholder="Search..." />
		</div>
	</div>

	<div class="grid gap-4 md:gap-8 md:grid-cols-2 lg:grid-cols-3 relative">
		{#each filteredPipelines as pipeline}
			<a
				class="text-left hover:variant-glass bg-surface-100 dark:variant-soft-surface dark:hover:bg-surface-800 p-4 shadow-lg space-y-4 flex flex-col"
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
