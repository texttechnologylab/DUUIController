<script lang="ts">
	import { equals, toTitleCase } from '$lib/utils/text'
	import type { Placement } from '@floating-ui/dom'
	import { faCheck, faChevronDown, type IconDefinition } from '@fortawesome/free-solid-svg-icons'
	import { ListBox, ListBoxItem, popup, type PopupSettings } from '@skeletonlabs/skeleton'
	import Fa from 'svelte-fa'

	export let label: string = ''
	export let name: string = label

	export let options: string[]
	export let value: string

	export let icon: IconDefinition = faChevronDown
	export let placement: Placement = 'bottom-end'
	export let offset: number = 8

	const dropdown: PopupSettings = {
		event: 'click',
		target: name,
		placement: placement,
		closeQuery: '.listbox-item',
		middleware: {
			offset: offset
		}
	}
</script>

<div class="label flex flex-col">
	{#if label}
		<span class="uppercase text-xs tracking-widest">{label} </span>
	{/if}
	<button
		class="flex justify-between items-center gap-2 px-3 py-2 leading-6 border-[1px] bg-white dark:bg-surface-600 border-surface-400/20 dark:border-surface-400/20"
		use:popup={dropdown}
	>
		<span>{toTitleCase(value)}</span>
		<Fa {icon} />
	</button>
</div>

<div
	class="shadow-xl border-[1px] bg-white dark:bg-surface-600 border-surface-400/20 dark:border-surface-400/20 z-10"
	data-popup={name}
>
	<ListBox rounded="rounded-none" spacing="space-y-0">
		{#each options as option}
			<ListBoxItem
				bind:group={value}
				{name}
				value={option}
				rounded="rounded-none"
				spacing="space-y-0"
				hover="bg-primary-hover-token"
				active="bg-primary-hover-token"
			>
				<svelte:fragment slot="lead">
					<Fa class={equals(value, option) ? '' : 'text-transparent'} icon={faCheck} />
				</svelte:fragment>
				{option}
			</ListBoxItem>
		{/each}
	</ListBox>
</div>
