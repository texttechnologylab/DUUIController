<script lang="ts">
	import { faEye, faEyeSlash } from '@fortawesome/free-solid-svg-icons'
	import Fa from 'svelte-fa'

	export let label: string = ''
	export let name: string = ''
	export let value: string = ''
	export let style: string = ''

	export let required: boolean = true
	export let readonly: boolean = false

	let input: HTMLInputElement
	let hidden: boolean = true

	const toggleVisibility = () => {
		hidden = !hidden
		input.type = hidden ? 'password' : 'text'
		input.readOnly = hidden
	}
</script>

<label class="label flex flex-col {style}">
	<span class="uppercase text-xs tracking-widest">{label}</span>
	<div
		class="flex items-center border-[1px] focus-within:ring-1 focus-within:ring-[#2563eb] focus-within:border-[#2563eb] bg-white dark:bg-surface-600 border-surface-400/20"
	>
		<input
			class="bg-transparent border-0 grow focus-within:ring-0"
			type="password"
			{name}
			bind:value
			bind:this={input}
			{required}
			{readonly}
		/>
		<button
			class="btn btn-sm bg-transparent appearance-none"
			on:click|preventDefault={toggleVisibility}
		>
			<Fa class="w-4 bg-transparent appearance-none" icon={hidden ? faEyeSlash : faEye} />
		</button>
	</div>
</label>
