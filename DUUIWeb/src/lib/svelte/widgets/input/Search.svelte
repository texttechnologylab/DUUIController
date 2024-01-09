<script lang="ts">
	import { faSearch, type IconDefinition } from '@fortawesome/free-solid-svg-icons'
	import Fa from 'svelte-fa'

	export let placeholder: string = ''
	export let query: string
	export let style: string = ''
	export let icon: IconDefinition | null = faSearch
</script>

<label class="label flex flex-col {style}">
	<div
		class="{icon
			? 'pl-3'
			: ''} rounded-md overflow-hidden flex items-center border-[1px] focus-within:ring-1 focus-within:ring-[#2563eb] focus-within:border-[#2563eb]
			 bg-white dark:bg-surface-600 border-surface-400/20"
	>
		{#if icon}
			<Fa {icon} />
		{/if}
		<input
			class="bg-transparent border-0 grow focus-within:ring-0"
			type="text"
			bind:value={query}
			on:keydown
			on:focusout
			{placeholder}
		/>
	</div>
</label>
