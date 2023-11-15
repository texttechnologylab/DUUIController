<script lang="ts">
	export let label: string
	export let name: string
	export let value: string = ''

	export let required: boolean = false
	export let error: string = ''
</script>

<label class="label flex flex-col">
	<span class="uppercase text-xs tracking-widest">{label}</span>
	{#if error}
		<span class="pl-1 text-xs text-error-500">{error}</span>
	{/if}
	<input
		class="border-[1px] bg-white dark:bg-surface-600 {error
			? 'border-error-500'
			: 'border-surface-400/20'}"
		type="text"
		{name}
		bind:value
		{required}
	/>
</label>
