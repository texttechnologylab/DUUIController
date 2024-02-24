<script lang="ts">
	import { faLightbulb } from '@fortawesome/free-solid-svg-icons'
	import { popup, type PopupSettings } from '@skeletonlabs/skeleton'
	import Fa from 'svelte-fa'
	import { v4 as uuidv4 } from 'uuid'

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

	const id = uuidv4()

	const helpPopup: PopupSettings = {
		event: 'hover',
		target: id,
		placement: 'bottom',
		middleware: {
			offset: 0
		}
	}
</script>

{#if help}
	<div data-popup={id} class="z-50">
		<div class="text-sm max-w-[50ch] variant-filled-primary rounded-md p-4 shadow-md">
			<div class="grid grid-cols-[auto_1fr] items-center gap-4">
				<Fa icon={faLightbulb} size="2x" />
				<p class="border-l pl-4">{help}</p>
			</div>
		</div>
		<div class="arrow !left-1/2 !-translate-x-1/2 variant-filled-primary" />
	</div>
{/if}

<label class="{label ? 'label' : ''} flex flex-col {hidden ? 'hidden' : ''} {style}">
	<span class="form-label flex-center-4">{label} </span>

	<input
		{disabled}
		{placeholder}
		class="input-wrapper {error ? '!border-error-500' : ''}"
		use:popup={helpPopup}
		type="text"
		{name}
		bind:value
		{required}
		{readonly}
		on:keydown
		on:change
		on:focusout
	/>
	{#if error}
		<span class="pl-1 text-xs text-error-500 font-bold">{error}</span>
	{/if}
</label>
