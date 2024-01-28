<script lang="ts">
	import {
		faAnglesLeft,
		faAnglesRight,
		faChevronLeft,
		faChevronRight
	} from '@fortawesome/free-solid-svg-icons'
	import { createEventDispatcher } from 'svelte'
	import IconButton from '../action/IconButton.svelte'
	import Dropdown from '../input/Dropdown.svelte'

	const dispatcher = createEventDispatcher()

	export let settings: PaginationSettings
	export let showJumpButtons: boolean = true

	const decrement = () => {
		settings.page = Math.max(settings.page - 1, 0)
		dispatcher('change')
	}

	const increment = () => {
		if (settings.total <= (settings.page + 1) * settings.limit) return
		settings.page += 1
		dispatcher('change')
	}

	const toFirst = () => {
		settings.page = 0
		dispatcher('change')
	}
	const toLast = () => {
		settings.page = Math.ceil(settings.total / settings.limit)
		dispatcher('change')
	}
</script>

<div
	class="grid justify-center md:flex items-center md:justify-between gap-4 text-sm md:text-base py-4"
>
	<Dropdown
		on:change={() => dispatcher('change')}
		bind:value={settings.limit}
		options={settings.sizes}
		style="input-wrapper !py-2"
	/>
	<div
		class="input-no-highlight !py-0 flex items-center justify-center bg-surface-100 dark:variant-soft-surface
			   border border-surface-200 dark:border-surface-500"
	>
		{#if showJumpButtons}
			<IconButton
				disabled={settings.page === 0}
				_class="bg-transparent text-primary-500 border-r border-surface-200 dark:border-surface-500 hover:variant-filled-primary"
				rounded="rounded-none"
				icon={faAnglesLeft}
				on:click={toFirst}
			/>
		{/if}
		<IconButton
			disabled={settings.page === 0}
			_class="bg-transparent text-primary-500 border-r border-surface-200 dark:border-surface-500 hover:variant-filled-primary"
			rounded="rounded-none"
			icon={faChevronLeft}
			on:click={decrement}
		/>
		{#if settings.total === 0}
			<p class="px-4">No results</p>
		{:else}
			<p class="px-4">
				{1 + settings.page * settings.limit}-{Math.min(
					(settings.page + 1) * settings.limit,
					settings.total
				)} of {settings.total}
			</p>
		{/if}
		<IconButton
			disabled={settings.page === Math.floor(settings.total / settings.limit)}
			_class="bg-transparent text-primary-500 border-l border-surface-200 dark:border-surface-500 hover:variant-filled-primary"
			rounded="rounded-none"
			icon={faChevronRight}
			on:click={increment}
		/>
		{#if showJumpButtons}
			<IconButton
				disabled={settings.page === Math.floor(settings.total / settings.limit)}
				_class="bg-transparent text-primary-500 border-l border-surface-200 dark:border-surface-500 hover:variant-filled-primary"
				rounded="rounded-none"
				icon={faAnglesRight}
				on:click={toLast}
			/>
		{/if}
	</div>
</div>
