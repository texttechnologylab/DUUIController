<script lang="ts">
	import { includes } from '$lib/duui/utils/text'
	import {
		faArrowRight,
		faChevronUp,
		faClose,
		faFilter,
		faPlus,
		faRefresh,
		faSearch
	} from '@fortawesome/free-solid-svg-icons'

	import { goto } from '$app/navigation'
	import { page } from '$app/stores'
	import { DUUIDrivers, type DUUIDriver } from '$lib/duui/component'
	import { usedDrivers } from '$lib/duui/pipeline'

	import { userSession } from '$lib/store.js'
	import PipelineCard from '$lib/svelte/widgets/duui/PipelineCard.svelte'
	import Dropdown from '$lib/svelte/widgets/input/Dropdown.svelte'
	import Search from '$lib/svelte/widgets/input/Search.svelte'
	import Fa from 'svelte-fa'
	import { popup, type PopupSettings } from '@skeletonlabs/skeleton'

	export let data

	let { pipelines, count } = data

	let searchOpen: boolean = false
	let searchText: string = ''
	let filteredPipelines = pipelines

	let limit: number = +($page.url.searchParams.get('limit') || '10')

	let unused: boolean = false

	let driverFilter: DUUIDriver | string = 'Any'

	$: {
		filteredPipelines = pipelines
			.filter(
				(pipeline) =>
					includes(pipeline.name + ' ' + pipeline.description, searchText) || !searchText
			)
			.filter(
				(pipeline) =>
					driverFilter === 'Any' || usedDrivers(pipeline).has(driverFilter as DUUIDriver)
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

	const mobileFilter: PopupSettings = {
		event: 'click',
		target: 'mobile-filter',
		placement: 'top-end',
		closeQuery: '',
		middleware: {
			offset: 4
		}
	}
</script>

<svelte:head>
	<title>Pipelines</title>
</svelte:head>

<!-- Mobile Menu -->

<div data-popup="mobile-filter" class="popup-solid space-y-2 z-50 p-4">
	<Dropdown
		name="driverMobile"
		icon={faChevronUp}
		placement="top-start"
		options={['Any'].concat(DUUIDrivers)}
		bind:value={driverFilter}
	/>

	<Search
		bind:query={searchText}
		icon={faSearch}
		placeholder="Search..."
		style="input-wrapper !rounded-none md:!rounded-sm p-4 md:p-3"
	/>
</div>

<div class="menu-mobile">
	<a class="button-mobile" href="/pipelines/editor">
		<Fa icon={faPlus} />
		<span class="text-xs md:text-base">Editor</span>
	</a>

	<button class="button-mobile" use:popup={mobileFilter}>
		<Fa icon={searchOpen ? faClose : faFilter} />
		<span class="text-xs md:text-base">Filter</span>
	</button>
</div>

<div class="h-full relative">
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
		<div class="grid relative pb-16">
			<div
				class="sticky top-0 bg-surface-50-900-token border-y p-4 border-color hidden md:block z-10"
			>
				<div class="grid md:flex items-center md:justify-between relative gap-4">
					<a class="button button-primary mr-auto" href="/pipelines/editor">
						<Fa icon={faPlus} />
						<span class="text-xs md:text-base">Editor</span>
					</a>
					<Dropdown
						name="driver"
						placement="bottom-end"
						options={['Any'].concat(DUUIDrivers)}
						bind:value={driverFilter}
					/>

					<Search
						bind:query={searchText}
						icon={faSearch}
						placeholder="Search..."
						style="input-wrapper !rounded-none md:!rounded-sm p-4 md:p-3"
					/>
				</div>
			</div>

			<div class="h-full sticky top-32">
				<div class="md:min-h-[800px] p-4 space-y-4">
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
		</div>
	{/if}
</div>
