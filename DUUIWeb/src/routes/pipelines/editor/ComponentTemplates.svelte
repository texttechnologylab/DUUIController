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

	let filter: DUUIDriverFilter = 'Any'

	const filterByDriver = () => {
		if (filter === 'Any') {
			return components
		} else {
			return filteredComponents.filter((component) => equals(component.driver, filter))
		}
	}

	$: {
		filteredComponents = components.filter(
			(component: DUUIComponent) =>
				includes(
					`${component.name} ${component.description} ${component.driver} ${component.tags.join(
						' '
					)}`,
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
		<div class="grid sm:grid-cols-2 items-end gap-4">
			<Dropdown bind:value={filter} options={DUUIDriverFilters} />
			<Search bind:query={searchText} icon={faSearch} placeholder="Search..." />
		</div>
	</div>
	<div class="grid md:grid-cols-2 xl:grid-cols-4 gap-4">
		{#each filteredComponents as component (component.oid)}
			<button
				class="card-fancy text-left grid min-h-[300px] items-start col-span-1"
				on:click={() => onSelect(component)}
			>
				<div class="flex items-start justify-between gap-4">
					<p class="text-lg font-bold break-words">{component.name}</p>
					<DriverIcon driver={component.driver} />
				</div>
				<p>{component.description}</p>
				<div class="flex flex-wrap gap-2 self-end">
					{#each component.tags.sort((a, b) => (a < b ? -1 : 1)) as tag}
						<span class="chip variant-ghost-primary">
							{tag}
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
