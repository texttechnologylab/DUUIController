<!--
	@component
	A component that can be used to hide sensitive information like API keys and passwords.
-->
<script lang="ts">
	import { faEye, faEyeSlash } from '@fortawesome/free-solid-svg-icons'
	import Fa from 'svelte-fa'

	export let label: string = ''
	export let name: string = ''
	export let value: string | null = ''
	export let style: string = ''

	export let readonly: boolean = false

	let hidden: boolean = true
	export let disabled: boolean = false

	const toggleVisibility = () => {
		hidden = !hidden
	}

	const cipher: string = 'x'.repeat(value ? 16 : 0)
</script>

<label class="label flex flex-col {style}">
	<span class="form-label">{label}</span>
	<div class="flex items-center relative">
		<button
			class="w-4 absolute left-4 top-1/2 -translate-y-1/2 bg-inherit"
			on:click|preventDefault={toggleVisibility}
			tabindex="-1"
		>
			<Fa class="w-4 transition-300" icon={hidden ? faEyeSlash : faEye} />
		</button>
		{#if hidden}
			<input
				{disabled}
				class="pl-12 bg-transparent border-0 grow p-3 input-wrapper"
				type="text"
				{name}
				value={cipher}
				readonly
			/>
		{:else}
			<input
				{disabled}
				class="pl-12 bg-transparent border-0 grow p-3 input-wrapper"
				type="text"
				{name}
				bind:value
				{readonly}
			/>
		{/if}
	</div>
</label>
