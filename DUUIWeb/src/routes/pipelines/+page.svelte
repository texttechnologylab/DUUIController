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
	import Dropdown from '$lib/svelte/components/Dropdown.svelte'
	import PipelineCard from '$lib/svelte/components/PipelineCard.svelte'
	import Search from '$lib/svelte/components/Search.svelte'
	import { popup, type PopupSettings } from '@skeletonlabs/skeleton'
	import Fa from 'svelte-fa'
	import { onMount } from 'svelte'

	export let data

	let { pipelines, count } = data

	let searchOpen: boolean = false
	let searchText: string = ''
	let filteredPipelines = pipelines

	let limit: number = +($page.url.searchParams.get('limit') || '50')

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
		const response = await fetch(`/api/pipelines/batch?limit=${limit + 10}`, {
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

<div data-popup="mobile-filter" class="z-50">
	<div class="flex flex-col bg-surface-50-900-token shadow-lg border border-color">
		<Dropdown
			rounded="!rounded-none"
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
</div>

<div class="menu-mobile">
	<a class="button-mobile" href="/pipelines/editor">
		<Fa icon={faPlus} />
		<span>New</span>
	</a>

	<button class="button-mobile" use:popup={mobileFilter}>
		<Fa icon={searchOpen ? faClose : faFilter} />
		<span>Filter</span>
	</button>
</div>

<div class="h-full relative">
	{#if pipelines.length === 0}
		<div class="h-full flex items-center justify-center">
			<div class="flex flex-col justify-center items-center">
				<div class="section-wrapper p-8 space-y-8">
					<h1 class="h2 font-bold">Your personal Pipeline collection</h1>

					<div>
						<p>After you create a Pipeline, you will find it here.</p>
					</div>
					<div class="flex justify-end">
						<a class="button-primary" href="/pipelines/editor">
							New
							<Fa icon={faArrowRight} />
						</a>
					</div>

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
						<span class="text-xs md:text-base">New</span>
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
						placeholder="Search"
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
