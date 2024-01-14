<script lang="ts">
	import DriverIcon from '$lib/svelte/DriverIcon.svelte'
	import { DUUIDriverFilters, type DUUIComponent, type DUUIDriverFilter } from '$lib/duui/component'
	import { faSearch } from '@fortawesome/free-solid-svg-icons'
	import { createEventDispatcher } from 'svelte'
	import Search from '$lib/svelte/widgets/input/Search.svelte'
	import { equals, includes } from '$lib/duui/utils/text'
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

<div class="space-y-8">
	<div class="md:flex justify-between items-end space-y-4 md:space-y-0">
		<h2 class="h2 font-bold">Templates</h2>
		<div class="grid md:grid-cols-2 items-end gap-4">
			<Dropdown bind:value={filter} options={DUUIDriverFilters} />
			<Search bind:query={searchText} icon={faSearch} placeholder="Search..." />
		</div>
	</div>
	<div class="grid md:grid-cols-2 xl:grid-cols-4 gap-4">
		{#each filteredComponents as component (component.oid)}
			<button
				class="card-fancy text-left grid grid-rows-[auto_1fr_auto] items-start"
				on:click={() => onSelect(component)}
			>
				<div class="flex items-start justify-between gap-4">
					<p class="text-lg font-bold break-words">{component.name}</p>
					<DriverIcon driver={component.settings.driver} />
				</div>
				<p>{component.description}</p>
				<div class="flex flex-wrap gap-2 mt-8">
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
