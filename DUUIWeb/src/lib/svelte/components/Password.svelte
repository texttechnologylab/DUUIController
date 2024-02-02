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
	export let disabled: boolean = false

	const toggleVisibility = () => {
		hidden = !hidden
		input.type = hidden ? 'password' : 'text'
	}
</script>

<div>
	<label class="label flex flex-col {style} relative">
		<span class="form-label">{label}</span>
		<input
			on:keydown
			{disabled}
			class="bg-transparent border-0 grow input-wrapper pr-[60px]"
			type="password"
			{name}
			bind:value
			bind:this={input}
			{required}
			{readonly}
		/>
		<div class="flex items-center absolute right-4 top-1/2 translate-y-[20%]">
			<button
				class=" text-surface-400 hover:text-black transition-colors"
				on:click|preventDefault={toggleVisibility}
				tabindex="-1"
			>
				<Fa class="w-4" icon={hidden ? faEyeSlash : faEye} />
			</button>
		</div>
	</label>
</div>
