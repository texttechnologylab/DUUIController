<script lang="ts">
	import { equals, toTitleCase } from '$lib/duui/utils/text'
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

	export let style: string = 'input-wrapper'
	export let rounded: string = 'rounded-md'
	export let border: string = 'border'

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
		type="button"
		class="flex items-center justify-between gap-2 px-3 py-2 leading-6 {border} {rounded} {style}"
		use:popup={dropdown}
	>
		<span>{toTitleCase('' + value)}</span>
		<Fa {icon} />
	</button>
</div>

<div class="popup-solid !m-0 overflow-hidden" data-popup={name}>
	<ListBox rounded="rounded-none" spacing="space-y-0">
		{#each options as option}
			<ListBoxItem
				on:change
				bind:group={value}
				{name}
				value={option}
				rounded="rounded-none"
				spacing="space-y-0"
				active="variant-filled-primary dark:variant-soft-primary"
			>
				<svelte:fragment slot="lead">
					<Fa class={equals('' + value, '' + option) ? '' : 'invisible'} icon={faCheck} />
				</svelte:fragment>
				{option}
			</ListBoxItem>
		{/each}
	</ListBox>
</div>
