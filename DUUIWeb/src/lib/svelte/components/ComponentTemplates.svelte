<!-- @component This component is used to display templates for DUUIComponents. -->
<script lang="ts">
	import { componentDrawerSettings } from '$lib/config'
	import { DUUIDriverFilters, type DUUIComponent, type DUUIDriverFilter } from '$lib/duui/component'
	import { equals, includes } from '$lib/duui/utils/text'
	import { successToast } from '$lib/duui/utils/ui'
	import DriverIcon from '$lib/svelte/components/DriverIcon.svelte'
	import Dropdown from '$lib/svelte/components/Input/Dropdown.svelte'
	import Search from '$lib/svelte/components/Input/Search.svelte'
	import { faSearch } from '@fortawesome/free-solid-svg-icons'
	import { getDrawerStore, getToastStore, type DrawerSettings } from '@skeletonlabs/skeleton'
	import { createEventDispatcher } from 'svelte'

	const dispatcher = createEventDispatcher()
	export let components: DUUIComponent[]

	const drawerStore = getDrawerStore()
	const toastStore = getToastStore()

	const onEdit = (component: DUUIComponent) => {
		const drawer: DrawerSettings = {
			id: 'component',
			...componentDrawerSettings,
			meta: { component: component, inEditor: false, example: false }
		}

		drawerStore.open(drawer)
	}

	const onSelect = (component: DUUIComponent) => {
		dispatcher('select', {
			component: component
		})

		toastStore.trigger(successToast(`Template ${component.name} added`))
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
		<h2 class="h2">Templates</h2>
		<div class="grid sm:grid-cols-2 items-end gap-4">
			<Dropdown bind:value={filter} options={DUUIDriverFilters} />
			<Search bind:query={searchText} icon={faSearch} placeholder="Search..." />
		</div>
	</div>
	<div class="grid lg:grid-cols-2 xl:grid-cols-3 gap-4">
		{#each filteredComponents as component (component.oid)}
			<button
				class="card-fancy !overflow-visible text-left grid min-h-[250px] items-start col-span-1"
				on:click={() => onSelect(component)}
			>
				<div class="flex flex-col md:flex-row items-start justify-between gap-4">
					<p class="text-lg font-bold break-words">{component.name}</p>
					<DriverIcon driver={component.driver} />
				</div>
				<p>{component.description}</p>
				<div class="flex flex-wrap gap-2 self-end">
					{#each component.tags.sort((a, b) => (a < b ? -1 : 1)) as tag}
						<span class="tag">
							{tag}
						</span>
					{/each}
				</div>
			</button>
		{/each}
	</div>
	<div>
		{#if filteredComponents.length === 0}
			<h2 class="py-32 text-center h2 mx-auto">No templates found</h2>
		{/if}
	</div>
</div>
