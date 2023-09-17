<script lang="ts">
	import { ListBox, ListBoxItem, popup, type PopupSettings } from '@skeletonlabs/skeleton'

	let comboboxValue: string
	export let options: string[]

	const popupCombobox: PopupSettings = {
		event: 'focus-click',
		target: 'popupCombobox',
		placement: 'bottom',
		closeQuery: '.listbox-item'
	}
</script>

<button class="btn variant-filled w-48 justify-between" use:popup={popupCombobox}>
	<span class="capitalize">{comboboxValue ?? 'Trigger'}</span>
	<span>↓</span>
</button>
<div class="card w-48 shadow-xl py-2" data-popup="popupCombobox">
	<ListBox rounded="rounded-none">
		{#each options as option}
			<ListBoxItem bind:group={comboboxValue} name={option} value={option}>{option}</ListBoxItem>
		{/each}
	</ListBox>
	<div class="arrow bg-surface-100-800-token" />
</div>
