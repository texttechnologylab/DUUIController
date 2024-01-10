<script lang="ts">
	import { faCheck, faChevronDown } from '@fortawesome/free-solid-svg-icons'
	import { ListBox, ListBoxItem, popup, type PopupSettings } from '@skeletonlabs/skeleton'
	import Fa from 'svelte-fa'

	export let label: string
	export let name: string

	export let selected: string[] = []
	export let options: string[]

	export let rows: number = 5
	export let offset: number = 8

	export let closeQuery: string = 'button'

	const columns: number = Math.floor(options.length / rows)

	const menu: PopupSettings = {
		event: 'click',
		target: name,
		placement: 'bottom-end',
		closeQuery: closeQuery,
		middleware: {
			offset: offset
		}
	}
</script>

<button
	class="flex items-center gap-2 px-3 py-2 leading-6 input-wrapper"
	use:popup={menu}
>
	<span class="grow text-left">{label}</span>
	<Fa icon={faChevronDown} />
</button>

<div
	class="overflow-hidden rounded-md shadow-lg border-[1px] bg-white dark:bg-surface-600 border-surface-400/20 z-50"
	data-popup={name}
>
	<ListBox {name} multiple={true} class="grid grid-cols-{columns}" spacing="space-y-0">
		{#each options as option}
			<ListBoxItem
				on:change
				bind:group={selected}
				name={option}
				value={option}
				rounded="rounded-none"
				spacing="space-y-0"
				hover="bg-primary-hover-token"
				active="bg-primary-hover-token"
			>
				<svelte:fragment slot="lead">
					<Fa class={selected.includes(option) ? '' : 'text-transparent'} icon={faCheck} />
				</svelte:fragment>
				{option}
			</ListBoxItem>
		{/each}
	</ListBox>
</div>
