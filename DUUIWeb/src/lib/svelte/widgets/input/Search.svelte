<script lang="ts">
	import { faSearch, type IconDefinition } from '@fortawesome/free-solid-svg-icons'
	import Fa from 'svelte-fa'

	export let placeholder: string = ''
	export let query: string
	export let style: string = ''
	export let icon: IconDefinition | null = faSearch
</script>

<label class="label flex flex-col {style}">
	<div class="flex items-center relative">
		{#if icon}
			<Fa {icon} class="absolute right-4 top-1/2 -translate-y-1/2 text-surface-400" />
		{/if}
		<input
			class="bg-transparent border-0 grow input-wrapper"
			type="text"
			bind:value={query}
			on:keydown
			on:focusout
			{placeholder}
		/>
	</div>
</label>
