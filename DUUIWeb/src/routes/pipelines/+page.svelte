<script lang="ts">
	import { blankPipeline } from '$lib/duui/pipeline'
	import { includes } from '$lib/duui/utils/text'
	import ActionButton from '$lib/svelte/widgets/action/ActionButton.svelte'
	import {
		faClose,
		faFileImport,
		faPlus,
		faSearch
	} from '@fortawesome/free-solid-svg-icons'

	import { goto } from '$app/navigation'
	import { currentPipelineStore } from '$lib/store.js'
	import PipelineCard from '$lib/svelte/widgets/duui/PipelineCard.svelte'
	import Search from '$lib/svelte/widgets/input/Search.svelte'
	import { FileButton } from '@skeletonlabs/skeleton'
	import Fa from 'svelte-fa'

	export let data

	let { pipelines } = data

	let searchOpen: boolean = false
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

		goto('/pipelines/editor?import=true')
	}

	$: {
		filteredPipelines = pipelines.filter(
			(pipeline) => includes(pipeline.name + ' ' + pipeline.description, searchText) || !searchText
		)

		filteredPipelines = filteredPipelines.filter((pipeline) => pipeline.timesUsed === 0 || !unused)
	}
</script>

<svelte:head>
	<title>Pipelines</title>
</svelte:head>

<div class="h-full">
	{#if pipelines.length === 0}
		<div class="h-full flex items-center justify-center">
			<div class="flex flex-col justify-center items-center">
				<div class="section-wrapper p-8 space-y-8">
					<h1 class="h1 font-bold">Let's get started</h1>
					<div>
						<p>Start by Creating your first Pipeline.</p>
						<p>After you create or import a Pipeline, you will find it here.</p>
					</div>
					<div class="grid grid-cols-2 gap-4 p">
						<ActionButton on:click={() => goto('/pipelines/editor')} icon={faPlus} text="Create" />
						<FileButton
							name="files"
							bind:files={importFiles}
							on:change={importPipeline}
							button="btn variant-filled-primary dark:variant-soft-primary w-full"
							accept=".json"
						>
							<Fa icon={faFileImport} />
							<span>Import</span>
						</FileButton>
					</div>
				</div>
			</div>
		</div>
	{:else}
		<div class="grid">
			<div
				class="page-wrapper bg-solid md:top-0 z-10 left-0 bottom-0 right-0 row-start-2 fixed md:sticky md:row-start-1"
			>
				<div class="grid md:flex items-center md:justify-between gap-4 relative">
					<div class="grid {searchOpen ? 'grid-cols-3' : 'grid-cols-3'} md:flex items-center gap-4">
						<ActionButton on:click={() => goto('/pipelines/editor')} icon={faPlus} text="New" />

						<ActionButton
							_class="md:!hidden"
							on:click={() => (searchOpen = !searchOpen)}
							icon={searchOpen ? faClose : faSearch}
							text="Search"
						/>
						<FileButton
							name="files"
							bind:files={importFiles}
							on:change={importPipeline}
							button="button-primary w-full"
							accept=".json"
						>
							<Fa icon={faFileImport} />
							<span>Import</span>
						</FileButton>
					</div>

					<div class="md:ml-auto {searchOpen ? 'block' : 'hidden md:block'}">
						<Search bind:query={searchText} icon={faSearch} placeholder="Search..." />
					</div>
				</div>
			</div>
			<div class="md:min-h-[800px] p-4 md:p-8 space-y-4">
				<h1 class="h1 font-bold mb-8">Pipelines</h1>
				<div class="grid gap-4 md:gap-8 md:grid-cols-3 relative">
					{#each filteredPipelines as pipeline}
						<a
							class="card-fancy grid grid-rows-[auto_1fr_80px] items-start"
							href="/pipelines/{pipeline.oid}"
						>
							<PipelineCard {pipeline} />
						</a>
					{/each}
				</div>
			</div>
		</div>
	{/if}
</div>
