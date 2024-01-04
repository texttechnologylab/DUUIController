<script lang="ts">
	import DriverIcon from '$lib/components/DriverIcon.svelte'
	import { DUUIDriverFilters, type DUUIComponent, type DUUIDriverFilter } from '$lib/duui/component'
	import { faSearch } from '@fortawesome/free-solid-svg-icons'
	import { createEventDispatcher } from 'svelte'
	import Search from '$lib/svelte/widgets/input/Search.svelte'
	import { equals, includes } from '$lib/utils/text'
	import Dropdown from '$lib/svelte/widgets/input/Dropdown.svelte'

	const dispatcher = createEventDispatcher()
	export let components: DUUIComponent[]

	const onSelect = (component: DUUIComponent) => {
		dispatcher('select', {
			component: component
		})
	}

	let searchText: string = ''
	let filteredComponents = components

	let filter: DUUIDriverFilter = 'All'

	const filterByDriver = () => {
		switch (filter) {
			case 'All': {
				return filteredComponents
			}
			default: {
				return filteredComponents.filter((component) => equals(component.settings.driver, filter))
			}
		}
	}

	$: {
		filteredComponents = components.filter(
			(component: DUUIComponent) =>
				includes(
					`${component.name} ${component.description} ${
						component.settings.driver
					} ${component.categories.join(' ')}`,
					searchText
				) || !searchText
		)

		filter = filter
		filteredComponents = filterByDriver()
	}
</script>

<div class="md:mt-16 items-start justify-start rounded-none container">
	<div class="md:flex justify-between items-end py-4 space-y-4">
		<h3 class="h3">Templates</h3>
		<div class="grid md:grid-cols-2 gap-4 items-end">
			<Dropdown bind:value={filter} options={DUUIDriverFilters} />
			<Search bind:query={searchText} icon={faSearch} placeholder="Search..." />
		</div>
	</div>
	<hr class="bg-surface-400/20 h-[1px] !border-0 rounded" />
	<div class="grid md:grid-cols-2 xl:grid-cols-3 gap-4 py-4">
		{#each filteredComponents as component (component.oid)}
			<button
				class="component-card text-left p-4 hover:variant-glass bg-surface-100 dark:variant-soft-surface dark:hover:bg-surface-800 shadow-lg space-y-4 grid grid-rows-3 items-start"
				on:click={() => onSelect(component)}
			>
				<div class="flex items-start justify-between gap-4">
					<p class="text-lg font-bold break-words">{component.name}</p>
					<DriverIcon driver={component.settings.driver} />
				</div>
				<p class="row-span-2">{component.description}</p>
				<div class="flex flex-wrap gap-2 p-2">
					{#each component.categories as category}
						<span class="chip variant-glass-primary">
							{category}
						</span>
					{/each}
				</div>
			</button>
		{/each}
	</div>
	<div>
		{#if filteredComponents.length === 0}
			<p class="py-32 text-center h3">No templates found</p>
		{/if}
	</div>
</div>

<style>
	.component-card {
		grid-template-rows: auto auto 1fr 1fr;
	}
</style>
