<!--
	@component
	A Dropdown component where one or more options can be selected.
-->
<script lang="ts">
	import { faCheck, faChevronDown, type IconDefinition } from '@fortawesome/free-solid-svg-icons'
	import { ListBox, ListBoxItem, popup, type PopupSettings } from '@skeletonlabs/skeleton'
	import Fa from 'svelte-fa'

	export let label: string
	export let name: string

	export let selected: string[] = []
	export let options: string[]

	export let rows: number = 5
	export let offset: number = 4

	export let closeQuery: string = 'button'
	export let style: string = 'input-wrapper'
	export let rounded: string = 'rounded-md'
	export let border: string = 'border'
	export let icon: IconDefinition = faChevronDown

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
	class="flex items-center justify-between gap-2 px-3 py-2 leading-6 {border} {rounded} {style} bg-fancy z-50 md:min-w-[220px]"
	use:popup={menu}
>
	<span class="grow text-left">{label}</span>
	<Fa {icon} />
</button>

<div class="popup-solid z-50 md:min-w-[220px]" data-popup={name}>
	<ListBox
		{name}
		multiple={true}
		class="grid grid-cols-{columns} overflow-hidden"
		spacing="space-y-0"
	>
		{#each options as option}
			<ListBoxItem
				on:change
				bind:group={selected}
				name={option}
				value={option}
				rounded="rounded-none"
				spacing="space-y-0"
				hover="hover:bg-surface-100-800-token"
				active=""
			>
				<svelte:fragment slot="lead">
					<Fa class={selected.includes(option) ? '' : 'text-transparent'} icon={faCheck} />
				</svelte:fragment>
				{option}
			</ListBoxItem>
		{/each}
	</ListBox>
</div>
