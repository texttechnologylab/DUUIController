<script lang="ts">
	import { faLightbulb } from '@fortawesome/free-solid-svg-icons'
	import { popup, type PopupSettings } from '@skeletonlabs/skeleton'
	import Fa from 'svelte-fa'

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
			offset: 0
		}
	}
</script>

{#if help}
	<div data-popup="helpPopup">
		<div class="text-sm z-50 max-w-[50ch] variant-filled-primary rounded-sm p-4 shadow-lg">
			<div class="grid grid-cols-[auto_1fr] items-center gap-4">
				<Fa icon={faLightbulb} size="2x" />
				<p class="border-l pl-4">{help}</p>
			</div>
		</div>
	</div>
{/if}

<label class="{label ? 'label' : ''} flex flex-col {hidden ? 'hidden' : ''} {style}">
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
