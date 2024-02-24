<script lang="ts">
	import { faLightbulb } from '@fortawesome/free-solid-svg-icons'
	import { popup, type PopupSettings } from '@skeletonlabs/skeleton'
	import Fa from 'svelte-fa'
	import { v4 as uuidv4 } from 'uuid'

	export let label: string = ''
	export let name: string
	export let value: number = 0

	export let min: number = 0
	export let max: number = 100
	export let step: number = 1

	export let required: boolean = false
	export let showRange: boolean = false
	export let style: string = ''
	export let help: string = ''

	const id = uuidv4()

	const helpPopup: PopupSettings = {
		event: 'hover',
		target: id,
		placement: 'bottom-start',
		middleware: {
			offset: 8
		}
	}
</script>

{#if help}
	<div data-popup={id} class="z-10">
		<div class="text-sm max-w-[50ch] variant-filled-primary rounded-md p-4 shadow-md">
			<div class="grid grid-cols-[auto_1fr] items-center gap-4">
				<Fa icon={faLightbulb} size="2x" />
				<p class="border-l pl-4">{help}</p>
			</div>
		</div>
		<div class="arrow !left-1/2 !-translate-x-1/2 variant-filled-primary" />
	</div>
{/if}

<label class="label flex flex-col {style}">
	{#if label}
		<span class="form-label">{label}{showRange ? ` (${min} - ${max})` : ''}</span>
	{/if}
	<input
		class="input-wrapper
        [appearance:textfield] [&::-webkit-outer-spin-button]:appearance-none [&::-webkit-inner-spin-button]:appearance-none"
		type="number"
		inputmode="numeric"
		{min}
		use:popup={helpPopup}
		{max}
		{step}
		{name}
		bind:value
		{required}
	/>
</label>
