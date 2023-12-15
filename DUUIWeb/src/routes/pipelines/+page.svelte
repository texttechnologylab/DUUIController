<script lang="ts">
	import ActionButton from '$lib/svelte/widgets/action/ActionButton.svelte'
	import DriverIcon from '$lib/components/DriverIcon.svelte'
	import { blankPipeline, usedDrivers } from '$lib/duui/pipeline'
	import { includes } from '$lib/utils/text'
	import {
		faFileImage,
		faFileImport,
		faPlus,
		faSearch,
		faWifi
	} from '@fortawesome/free-solid-svg-icons'

	import Fa from 'svelte-fa'
	import TextInput from '$lib/svelte/widgets/input/TextInput.svelte'
	import { goto } from '$app/navigation'
	import { FileButton } from '@skeletonlabs/skeleton'
	import { currentPipelineStore } from '$lib/store.js'
	import { blankComponent } from '$lib/duui/component.js'

	export let data

	let { pipelines } = data

	let searchText: string = ''
	let filteredPipelines = pipelines

	let activeOnly: boolean = false
	let unused: boolean = false

	let importFiles: FileList

	const importPipeline = async () => {
		if (!importFiles) return
		const file: File = importFiles[0]
		const pipeline = JSON.parse(await file.text())
		$currentPipelineStore = blankPipeline()
		$currentPipelineStore.name = pipeline.name
		$currentPipelineStore.description = pipeline.description
		$currentPipelineStore.components = pipeline.components
		$currentPipelineStore.settings = pipeline.settings

		goto('/pipelines/new?import=true')
	}

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
	<hr class="bg-surface-400/20 h-[1px] !border-0 rounded" />

	<div class="grid md:flex items-center md:justify-between gap-4">
		<div class="flex items-center gap-4">
			<ActionButton on:click={() => goto('/pipelines/new')} icon={faPlus} text="Create" />
			<FileButton
				name="files"
				bind:files={importFiles}
				on:change={importPipeline}
				button="btn variant-filled-primary dark:variant-soft-primary rounded-none"
				accept=".json"
			>
				<Fa icon={faFileImport} />
				<span>Import</span>
			</FileButton>
			<!-- <ActionButton
				text="Import"
				icon={faFileImport}
				on:click={imn}
			/> -->
		</div>

		<div class="md:ml-auto">
			<TextInput bind:value={searchText} icon={faSearch} placeholder="Search..." />
		</div>
	</div>

	<div class="grid gap-4 md:gap-8 md:grid-cols-2 lg:grid-cols-3 relative">
		{#each filteredPipelines as pipeline}
			<a
				class="pipeline-card text-left hover:variant-glass bg-surface-100 dark:variant-soft-surface dark:hover:bg-surface-800 p-4 shadow-lg space-y-4
				 grid grid-rows-4 items-start"
				href="/pipelines/{pipeline.oid}"
			>
				<div class="flex items-center gap-4 justify-between">
					<h4 class="h4 font-bold">{pipeline.name}</h4>
					{#if pipeline.serviceStartTime !== 0}
						<Fa icon={faWifi} size="lg" class="text-success-500" />
					{/if}
				</div>

				<p class="row-span-2">{pipeline.description}</p>
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
	.pipeline-card {
		grid-template-rows: auto 1fr 1fr 1fr;
	}
</style>
