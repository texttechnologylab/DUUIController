<script lang="ts">
	import { faCheck, faChevronDown } from '@fortawesome/free-solid-svg-icons'
	import { ListBox, ListBoxItem, popup, type PopupSettings } from '@skeletonlabs/skeleton'
	import Fa from 'svelte-fa'

	export let value: string = ''
	export let values: string[] = []
	export let rows: number = 5
	export let multiple: boolean = false
	export let options: string[]
	export let text: string = ''
	export let closeQuery: string = '.listbox-item'
	export let id: string
	export let name: string = ''

	const columns: number = Math.floor(options.length / rows)

	const combobox: PopupSettings = {
		event: 'click',
		target: id,
		placement: 'bottom-end',
		closeQuery: closeQuery,
		middleware: {
			offset: 8
		}
	}
</script>

<div class="flex flex-col justify-start gap-1">
	{#if name}
		<span class="uppercase text-xs tracking-widest">{name}</span>
	{/if}
	<button
		class="p-2 px-4 gap-4 rounded-none flex items-center border-[1px] border-surface-600 focus-within:border-primary-500"
		use:popup={combobox}
	>
		<span class="grow text-left">{multiple ? text : value}</span>
		<Fa icon={faChevronDown} />
	</button>
</div>

<div
	class="dark:bg-surface-700 variant-glass items-center border-[1px] border-surface-600 focus-within:border-primary-500 z-10"
	data-popup={id}
>
	<ListBox
		{id}
		{multiple}
		rounded="rounded-none"
		active="bg-transparent"
		class="grid grid-cols-{columns}"
		spacing="0"
	>
		{#each options as option}
			{#if multiple}
				<ListBoxItem bind:group={values} name={option} value={option}>
					<svelte:fragment slot="lead">
						<Fa class={values.includes(option) ? '' : 'text-transparent'} icon={faCheck} />
					</svelte:fragment>
					{option}
				</ListBoxItem>
			{:else}
				<ListBoxItem bind:group={value} name={option} value={option}>
					<svelte:fragment slot="lead">
						<Fa class={value === option ? '' : 'text-transparent'} icon={faCheck} />
					</svelte:fragment>
					{option}
				</ListBoxItem>
			{/if}
		{/each}
	</ListBox>
</div>
