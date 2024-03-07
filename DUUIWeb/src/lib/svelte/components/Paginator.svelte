<!--
	 @component 
	A component used for pagination in tables. Uses the PaginationSettings type.
 -->
<script lang="ts">
	import {
		faAngleLeft,
		faAngleRight,
		faAnglesLeft,
		faAnglesRight
	} from '@fortawesome/free-solid-svg-icons'
	import { createEventDispatcher } from 'svelte'
	import Fa from 'svelte-fa'
	import Dropdown from './Input/Dropdown.svelte'

	const dispatcher = createEventDispatcher()

	export let settings: PaginationSettings

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
		let page = Math.floor(settings.total / settings.limit)
		let first = settings.limit * page + 1

		if (first > settings.total) {
			page -= 1
		}

		settings.page = page
		dispatcher('change')
	}
</script>

<div class="justify-between flex gap-4 text-sm md:text-base">
	<Dropdown
		on:change={() => dispatcher('change')}
		bind:value={settings.limit}
		options={settings.sizes}
		border="bordered-soft"
		style="shadow-md grow bg-surface-50-900-token self-stretch"
	/>
	<div
		class="input-no-highlight bordered-soft !bg-surface-50-900-token !py-0 flex justify-center !rounded-md shadow-md overflow-hidden"
	>
		<div class="grid grid-cols-2">
			<button
				disabled={settings.page === 0}
				on:click={toFirst}
				class="button-neutral !aspect-square !border-none !rounded-none !justify-center"
			>
				<Fa icon={faAnglesLeft} />
			</button>
			<button
				disabled={settings.page === 0}
				on:click={decrement}
				class="button-neutral !aspect-square !border-none !rounded-none !justify-center"
			>
				<Fa icon={faAngleLeft} />
			</button>
		</div>
		<div class="px-4 self-center min-w-max">
			{#if settings.total === 0}
				<p>No results</p>
			{:else}
				<p>
					{1 + settings.page * settings.limit}-{Math.min(
						(settings.page + 1) * settings.limit,
						settings.total
					)} of {settings.total}
				</p>
			{/if}
		</div>
		<div class="grid grid-cols-2">
			<button
				disabled={(settings.page + 1) * settings.limit + 1 > settings.total}
				on:click={increment}
				class="button-neutral !aspect-square !border-none !rounded-none !justify-center"
			>
				<Fa icon={faAngleRight} />
			</button>
			<button
				disabled={(settings.page + 1) * settings.limit + 1 > settings.total}
				on:click={toLast}
				class="button-neutral !aspect-square !border-none !rounded-none !justify-center"
			>
				<Fa icon={faAnglesRight} />
			</button>
		</div>
	</div>
</div>
