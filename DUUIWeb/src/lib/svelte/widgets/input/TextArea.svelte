<script lang="ts">
	export let label: string
	export let name: string

	export let value: string = ''
	export let error: string = ''
	export let style: string = ''

	export let required: boolean = false
</script>

<label class="label flex flex-col {style}">
	<span class="uppercase text-xs tracking-widest">{label}</span>
	{#if error}
		<span class="pl-1 text-xs text-error-500">{error}</span>
	{/if}
	<textarea
		class="rounded-md border-[1px] bg-white dark:bg-surface-600 {error
			? 'border-error-500'
			: 'border-surface-400/20'}"
		{required}
		{name}
		rows={4}
		bind:value
		on:keydown
		on:focusout
	/>
</label>
