<script lang="ts">
	import DriverIcon from '$lib/components/DriverIcon.svelte'
	import type { DUUIComponent } from '$lib/duui/component'
	import { faSearch } from '@fortawesome/free-solid-svg-icons'
	import { createEventDispatcher } from 'svelte'
	import TextInput from '$lib/svelte/widgets/input/TextInput.svelte'
	import Search from '$lib/svelte/widgets/input/Search.svelte'
	import { includes } from '$lib/utils/text'

	const dispatcher = createEventDispatcher()
	export let components: DUUIComponent[]

	const onSelect = (component: DUUIComponent) => {
		dispatcher('select', {
			component: component
		})
	}

	let searchText: string = ''
	let filteredComponents = components

	$: {
		filteredComponents = components.filter(
			(component: DUUIComponent) =>
				includes(
					`${component.name} ${component.description} ${component.categories.join(' ')}`,
					searchText
				) || !searchText
		)
	}
</script>

<div class="md:mt-16 items-start justify-start rounded-none container">
	<div class="md:flex space-y-4 justify-between items-center p-4">
		<h3 class="h3">Templates</h3>

		<Search bind:query={searchText} icon={faSearch} placeholder="Search..." />
	</div>
	<hr class="bg-surface-400/20 h-[1px] !border-0 rounded " />
	<div class="grid sm:grid-cols-2 md:grid-cols-3 gap-4 p-4">
		{#each filteredComponents as component (component.oid)}
			<button
				class="text-left p-4 hover:variant-glass bg-surface-100 dark:variant-soft-surface dark:hover:bg-surface-800 shadow-lg space-y-4 md:flex flex-col"
				on:click={() => onSelect(component)}
			>
				<div class="flex items-center justify-between gap-4">
					<p class="text-lg font-bold break-words">{component.name}</p>
					<DriverIcon driver={component.settings.driver} />
				</div>
				<p class="grow">{component.description}</p>
				<div class="flex flex-wrap gap-2 p-2">
					{#each component.categories as category}
						<!-- svelte-ignore a11y-no-static-element-interactions -->
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

	<!-- <footer class="flex flex-col">
		<div class="flex justify-between items-center  p-4">
			<ActionButton
				text="Clear selection"
				icon={faClose}
				variant="variant-filled-error dark:variant-soft-error"
				_class={selectedComponents.length > 0 ? 'inline-flex' : 'hidden'}
				on:click={onClear}
			/>
			<ActionButton
				text="Confirm"
				icon={faCheck}
				variant="variant-filled-success dark:variant-soft-success"
				_class="ml-auto"
				on:click={onSelect}
			/>
		</div>
	</footer> -->
</div>
