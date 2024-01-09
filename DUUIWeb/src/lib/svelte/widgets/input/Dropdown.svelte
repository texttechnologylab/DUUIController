<script lang="ts">
	import { equals, toTitleCase } from '$lib/utils/text'
	import type { Placement } from '@floating-ui/dom'
	import { faCheck, faChevronDown, type IconDefinition } from '@fortawesome/free-solid-svg-icons'
	import { ListBox, ListBoxItem, popup, type PopupSettings } from '@skeletonlabs/skeleton'
	import Fa from 'svelte-fa'

	export let label: string = ''
	export let name: string = label

	export let options: string[] | number[]
	export let value: string | number

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
		<span class="form-label">{label} </span>
	{/if}
	<button
		class="rounded-md flex justify-between items-center gap-2 px-3 py-2 leading-6 input-wrapper"
		use:popup={dropdown}
	>
		<span>{toTitleCase('' + value)}</span>
		<Fa {icon} />
	</button>
</div>

<div class="input-wrapper shadow-lg z-10" data-popup={name}>
	<ListBox rounded="rounded-none" spacing="space-y-0">
		{#each options as option}
			<ListBoxItem
				on:change
				bind:group={value}
				{name}
				value={option}
				rounded="rounded-none"
				spacing="space-y-0"
				hover="bg-primary-hover-token"
				active="bg-primary-hover-token"
			>
				<svelte:fragment slot="lead">
					<Fa class={equals('' + value, '' + option) ? '' : 'text-transparent'} icon={faCheck} />
				</svelte:fragment>
				{option}
			</ListBoxItem>
		{/each}
	</ListBox>
</div>
