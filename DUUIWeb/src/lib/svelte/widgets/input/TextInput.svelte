<script lang="ts">
	import { popup, type PopupSettings } from '@skeletonlabs/skeleton'

	export let label: string = ''
	export let name: string = ''
	export let value: string = ''

	export let required: boolean = false
	export let readonly: boolean = false
	export let error: string = ''
	export let hidden: boolean = false
	export let style: string = ''
	export let disabled: boolean = false
	export let placeholder: string = ''
	export let help: string = ''

	const helpPopup: PopupSettings = {
		event: 'hover',
		target: 'helpPopup',
		placement: 'bottom-start',
		middleware: {
			offset: 8
		}
	}
</script>

{#if help}
	<div
		class="text-sm z-50 break-words max-w-[50ch] variant-filled-primary rounded-md p-4 shadow-lg
		space-y-4"
		data-popup="helpPopup"
	>
		<p>{help}</p>
	</div>
{/if}

<label class="label flex flex-col {hidden ? 'hidden' : ''} {style}">
	<span class="form-label flex items-center gap-4">{label} </span>

	{#if error}
		<span class="pl-1 text-xs text-error-500 font-bold">{error}</span>
	{/if}
	<input
		{disabled}
		{placeholder}
		class="input-wrapper {error ? 'input-error' : ''}"
		use:popup={helpPopup}
		type="text"
		{name}
		bind:value
		{required}
		{readonly}
		on:keydown
	/>
</label>
