<script lang="ts">
	import ActionButton from '$lib/svelte/widgets/action/ActionButton.svelte'
	import DriverIcon from '$lib/components/DriverIcon.svelte'
	import { blankPipeline, usedDrivers } from '$lib/duui/pipeline'
	import { includes } from '$lib/duui/utils/text'
	import {
		faFileImage,
		faFileImport,
		faPlus,
		faSearch,
		faWifi
	} from '@fortawesome/free-solid-svg-icons'

	import Fa from 'svelte-fa'
	import { goto } from '$app/navigation'
	import { FileButton } from '@skeletonlabs/skeleton'
	import { currentPipelineStore } from '$lib/store.js'
	import Search from '$lib/svelte/widgets/input/Search.svelte'

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

		goto('/pipelines/editor?import=true')
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

<div class="h-full container mx-auto flex flex-col gap-4 md:my-4">
	{#if pipelines.length === 0}
		<div class="flex flex-col justify-center items-center h-full">
			<div class="section-wrapper p-8 space-y-8">
				<h1 class="h2 font-bold">Let's get started</h1>
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
						<span>Import</span>
						<Fa icon={faFileImport} />
					</FileButton>
				</div>
			</div>
		</div>
	{:else}
		<h1 class="h2">Pipelines</h1>
		<hr class="bg-surface-400/20 h-[1px] !border-0 rounded" />

		<div class="grid md:flex items-center md:justify-between gap-4 relative">
			<div class="flex items-center gap-4">
				<ActionButton on:click={() => goto('/pipelines/editor')} icon={faPlus} text="Create" />
				<FileButton
					name="files"
					bind:files={importFiles}
					on:change={importPipeline}
					button="btn variant-filled-primary dark:variant-soft-primary"
					accept=".json"
				>
					<span>Import</span>
					<Fa icon={faFileImport} />
				</FileButton>
			</div>

			<div class="md:ml-auto">
				<Search bind:query={searchText} icon={faSearch} placeholder="Search..." />
			</div>
		</div>

		<div class="grid gap-4 md:gap-8 md:grid-cols-2 lg:grid-cols-3 relative">
			{#each filteredPipelines as pipeline}
				<a class="card-fancy grid grid-rows-[auto_1fr_80px]" href="/pipelines/{pipeline.oid}">
					<div class="flex items-center gap-4 justify-between">
						<h4 class="h4 font-bold">{pipeline.name}</h4>
						{#if pipeline.user_id === null || pipeline.user_id === undefined}
							<p class="badge variant-soft-tertiary">Template</p>
						{/if}
					</div>

					<p class="row-span-2">{pipeline.description}</p>
					<div class="pt-4 flex items-center justify-between self-end">
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
	{/if}
</div>
