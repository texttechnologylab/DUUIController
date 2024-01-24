<script lang="ts">
	import { blankPipeline } from '$lib/duui/pipeline'
	import { includes } from '$lib/duui/utils/text'
	import {
		faArrowRight,
		faCirclePlus,
		faClose,
		faPlus,
		faRefresh,
		faSearch
	} from '@fortawesome/free-solid-svg-icons'

	import { goto } from '$app/navigation'
	import { page } from '$app/stores'
	import { currentPipelineStore, userSession } from '$lib/store.js'
	import PipelineCard from '$lib/svelte/widgets/duui/PipelineCard.svelte'
	import Search from '$lib/svelte/widgets/input/Search.svelte'
	import Fa from 'svelte-fa'

	export let data

	let { pipelines, count } = data

	let searchOpen: boolean = false
	let searchText: string = ''
	let filteredPipelines = pipelines

	let limit: number = +($page.url.searchParams.get('limit') || '10')

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

		filteredPipelines = filteredPipelines.filter((pipeline) => pipeline.times_used === 0 || !unused)
	}

	let loading: boolean = false

	const loadMore = async () => {
		loading = true
		const response = await fetch(`/api/pipelines?limit=${limit + 10}`, {
			method: 'GET'
		})

		if (response.ok) {
			goto(`/pipelines?limit=${limit + 10}`)
			const result = await response.json()
			pipelines = result.pipelines
			count = result.count
		}
	}

	const updateUser = async (data: object) => {
		const response = await fetch('/api/users', { method: 'PUT', body: JSON.stringify(data) })
		return response
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
					<h1 class="h2 font-bold">Your personal Pipeline collection</h1>
					{#if $userSession?.preferences.tutorial && $userSession.preferences.step === 1}
						<p>
							In the future you will find all your Pipelines here. Everytime you want to start an
							analysis process, update, or delete something, come back here.
						</p>
						<div class="flex justify-end">
							<a
								class="button-primary"
								href="/pipelines/editor"
								on:click={() => {
									updateUser({ 'preferences.step': 2 })
									$userSession.preferences.step = 2
								}}
							>
								Continue
								<Fa icon={faArrowRight} />
							</a>
						</div>
					{:else}
						<div>
							<p>After you create a Pipeline, you will find it here.</p>
						</div>
						<div class="flex justify-end">
							<a class="button-primary" href="/pipelines/editor">
								Editor
								<Fa icon={faArrowRight} />
							</a>
						</div>
					{/if}

					<!-- <div class="grid grid-cols-2 gap-4 p"> -->

					<!-- <FileButton
							name="files"
							bind:files={importFiles}
							on:change={importPipeline}
							button="button-primary w-full"
							accept=".json"
						>
							<Fa icon={faFileImport} />
							<span>Import</span>
						</FileButton> -->
					<!-- </div> -->
				</div>
			</div>
		</div>
	{:else}
		<div class="grid">
			<div
				class="page-wrapper bg-solid md:top-0 z-10 left-0 bottom-0 right-0 row-start-2 fixed md:sticky md:row-start-1"
			>
				<div class="grid md:flex items-center md:justify-between relative">
					<div
						class="grid {searchOpen ? 'grid-cols-3' : 'grid-cols-3'} md:flex items-center md:gap-4"
					>
						<a class="button button-primary" href="/pipelines/editor">
							<Fa icon={faPlus} />
							<span class="text-xs md:text-base">Editor</span>
						</a>

						<button class="button button-primary" on:click={() => (searchOpen = !searchOpen)}>
							<Fa icon={searchOpen ? faClose : faSearch} />
							<span class="text-xs md:text-base">Search</span>
						</button>
						<!-- 
						<FileButton
							name="files"
							bind:files={importFiles}
							on:change={importPipeline}
							button="button-primary w-full"
							accept=".json"
						>
							<Fa icon={faFileImport} />
							<span class="text-xs md:text-base">Import</span>
						</FileButton> -->
					</div>

					<div class="md:ml-auto {searchOpen ? 'block' : 'hidden md:block'}">
						<Search
							bind:query={searchText}
							icon={faSearch}
							placeholder="Search..."
							style="input-wrapper !rounded-none md:!rounded-md p-4 md:p-3"
						/>
					</div>
				</div>
			</div>
			<div class="md:min-h-[800px] p-4 md:p-8 space-y-4">
				<h1 class="h1 font-bold mb-8">Pipelines</h1>
				<div class="grid md:grid-cols-2 xl:grid-cols-3 gap-4 md:gap-8 relative">
					{#each filteredPipelines as pipeline}
						<a class="card-fancy grid items-start min-h-[300px]" href="/pipelines/{pipeline.oid}">
							<PipelineCard {pipeline} />
						</a>
					{/each}
					{#if count - pipelines.length > 0}
						<div class="flex items-center justify-center row-span-4">
							<button class="button-primary {loading ? 'aspect-square' : ''}" on:click={loadMore}>
								<Fa icon={faRefresh} spin={loading} />
								{#if !loading}
									<span>Load more</span>
								{/if}
							</button>
						</div>
					{/if}
				</div>
			</div>
		</div>
	{/if}
</div>
