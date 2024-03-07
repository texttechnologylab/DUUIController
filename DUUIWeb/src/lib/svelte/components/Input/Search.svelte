<!--
	@component
	A component representing a search field.
-->
<script lang="ts">
	import { faSearch, type IconDefinition } from '@fortawesome/free-solid-svg-icons'
	import Fa from 'svelte-fa'

	export let placeholder: string = ''
	export let query: string
	export let style: string = 'input-wrapper'
	export let icon: IconDefinition | null = faSearch
</script>

<label class="label flex flex-col">
	<div class="flex items-center relative">
		{#if icon}
			<Fa {icon} class="absolute right-4 top-1/2 -translate-y-1/2 text-surface-400 " />
		{/if}
		<input
			class="bg-transparent border-0 grow duration-300 transition-all focus-within:pr-32 {style} "
			type="text"
			bind:value={query}
			on:keydown
			on:keyup
			on:focusout
			{placeholder}
		/>
	</div>
</label>
