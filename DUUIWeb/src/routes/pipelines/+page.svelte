<script lang="ts">
	import { includes } from '$lib/duui/utils/text'
	import {
		faArrowDownWideShort,
		faArrowUpWideShort,
		faChevronRight,
		faChevronUp,
		faClose,
		faFilter,
		faPlus,
		faQuestion,
		faRefresh,
		faSearch,
		faSort
	} from '@fortawesome/free-solid-svg-icons'

	import { goto } from '$app/navigation'
	import { page } from '$app/stores'
	import { DUUIDrivers, type DUUIDriver } from '$lib/duui/component'
	import { Status } from '$lib/duui/monitor.js'
	import { usedDrivers } from '$lib/duui/pipeline'
	import Dropdown from '$lib/svelte/components/Input/Dropdown.svelte'
	import Search from '$lib/svelte/components/Input/Search.svelte'
	import PipelineCard from '$lib/svelte/components/PipelineCard.svelte'
	import { popup, type PopupSettings } from '@skeletonlabs/skeleton'
	import { onMount } from 'svelte'
	import Fa from 'svelte-fa'

	export let data

	let { pipelines, count } = data

	let searchOpen: boolean = false
	let searchText: string = ''
	let filteredPipelines = pipelines

	const paginationSettings: PaginationSettings = {
		limit: 12,
		page: 0,
		total: count,
		sizes: [20, 50]
	}

	const sort: Sort = {
		index: 0,
		order: -1
	}

	paginationSettings.limit = +($page.url.searchParams.get('limit') || '12')

	let unused: boolean = false

	let driverFilter: DUUIDriver | string = 'Any'

	onMount(() => {
		if ($page.url.searchParams.get('id')) {
			goto(`/pipelines/${$page.url.searchParams.get('id')}`)
		}
	})

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
		paginationSettings.limit += 12
		const response = await fetch(`/api/pipelines/batch?limit=${paginationSettings.limit}`, {
			method: 'GET'
		})

		if (response.ok) {
			goto(`/pipelines
			?limit=${paginationSettings.limit}
			&skip=${paginationSettings.page * paginationSettings.limit}
			&sort=${sortCriteria.at(sort.index)}
			&order=${sort.order}`)
			const result = await response.json()
			pipelines = result.pipelines
			count = result.count
		}

		loading = false
	}

	const sortCriteria = ['created_at', 'name', 'times_used']
	const sortCriteriaNames = ['Created At', 'Name', 'Times Used']

	const sortPipelines = async () => {
		const response = await fetch(
			`/api/pipelines/batch
			?limit=${paginationSettings.limit}
			&skip=${paginationSettings.page * paginationSettings.limit}
			&sort=${sortCriteria.at(sort.index)}
			&order=${sort.order}`
		)

		if (response.ok) {
			const json = await response.json()
			pipelines = json.pipelines
			goto(`/pipelines
			?limit=${paginationSettings.limit}
			&skip=${paginationSettings.page * paginationSettings.limit}
			&sort=${sortCriteria.at(sort.index)}
			&order=${sort.order}`)
		}
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

	const sortPopup: PopupSettings = {
		event: 'click',
		target: 'sort-popup',
		placement: 'top-start',
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

<div data-popup="mobile-filter" class="z-50">
	<div class="popup-solid p-2 space-y-2">
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
			style="input-wrapper p-4 md:p-3"
		/>
	</div>
</div>

<div class="menu-mobile">
	<a class="button-mobile" href="/pipelines/build">
		<Fa icon={faPlus} />
		<span>New</span>
	</a>

	<button class="button-mobile" use:popup={sortPopup}>
		<Fa icon={faSort} />
		<span>Sort</span>
	</button>

	<button class="button-mobile" use:popup={mobileFilter}>
		<Fa icon={searchOpen ? faClose : faFilter} />
		<span>Filter</span>
	</button>
</div>

<div data-popup="sort-popup" class="z-50">
	<div class="popup-solid p-2 space-y-2 grid">
		{#each sortCriteriaNames as criteria, index}
			<button
				class="button-neutral !border-none !justify-start"
				on:click={() => {
					if (sort.index === index) {
						sort.order *= -1
					} else {
						sort.order = 1
					}
					sort.index = index
					sortPipelines()
				}}
			>
				<Fa
					icon={sort.order === -1 ? faArrowDownWideShort : faArrowUpWideShort}
					class={index === sort.index ? 'visible' : 'invisible'}
				/>
				{criteria}
			</button>
		{/each}
	</div>
</div>

<div class="h-full relative">
	{#if pipelines.length === 0}
		<div class="h-full flex items-center justify-center">
			<div class="flex flex-col justify-center items-center">
				<div class="section-wrapper p-8 md:p-32 space-y-8 md:space-y-16 text-center">
					<h1 class="h1">No pipelines yet</h1>
					<div class="flex justify-center">
						<a class="button-primary cta box-shadow" href="/pipelines/build">
							<span>Pipeline Builder</span>
							<Fa icon={faChevronRight} />
						</a>
					</div>
					<p>After you create a Pipeline, you will find it here.</p>
				</div>
			</div>
		</div>
	{:else}
		<div class="grid relative pb-16 isolate">
			<div class="sticky top-0 bg-surface-50-900-token border-b border-color hidden md:block z-10">
				<div class="grid md:flex items-stretch md:justify-between relative">
					<a class="anchor-menu mr-auto border-r border-color" href="/pipelines/build">
						<Fa icon={faPlus} />
						<span class="text-xs md:text-base">New</span>
					</a>
					<button
						class="button-menu inline-flex gap-4 items-center px-4 border-x border-color"
						use:popup={sortPopup}
					>
						<Fa icon={faSort} />
						<span>Sort</span>
					</button>

					<Dropdown
						name="driver"
						style="button-menu"
						border="border-none"
						placement="bottom-end"
						options={['Any'].concat(DUUIDrivers)}
						bind:value={driverFilter}
					/>

					<Search
						bind:query={searchText}
						icon={faSearch}
						placeholder="Search"
						style="input-no-highlight !border-y-0 !border-r-0 !border-l border-color !rounded-none !bg-transparent duration-300 transition-all focus-within:pr-32 focus-within:!bg-surface-50-900-token"
					/>
				</div>
			</div>
			{#if filteredPipelines.length === 0}
				<div class="h-full flex items-center justify-center p-16">
					<div
						class="section-wrapper px-32 p-16 space-y-4 text-center flex items-center justify-center flex-col"
					>
						<h1 class="h2">Nothing found</h1>
						<Fa icon={faQuestion} size="2x" />
					</div>
				</div>
			{:else}
				<div class="h-full sticky top-32">
					<div class="md:min-h-[800px] container mx-auto p-4 space-y-4">
						<div class="grid md:grid-cols-2 xl:grid-cols-3 gap-4 md:gap-8 relative">
							{#each filteredPipelines as pipeline}
								<a
									class="card-fancy {pipeline.status === Status.Idle
										? '!border-l-8 !border-l-success-500'
										: ''} grid items-start min-h-[300px]"
									href="/pipelines/{pipeline.oid}"
								>
									<PipelineCard {pipeline} />
								</a>
							{/each}
						</div>
						{#if count - pipelines.length > 0}
							<div class="flex items-center justify-center py-16">
								<button
									disabled={loading}
									class="button-primary {loading ? 'aspect-square' : ''}"
									on:click={loadMore}
								>
									<Fa icon={faRefresh} spin={loading} />
									{#if !loading}
										<span>Load more</span>
									{/if}
								</button>
							</div>
						{/if}
					</div>
				</div>
			{/if}
		</div>
	{/if}
</div>
