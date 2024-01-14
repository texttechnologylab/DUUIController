<script lang="ts">
	export let label: string
	export let name: string

	export let value: string = ''
	export let error: string = ''
	export let style: string = ''

	export let required: boolean = false
</script>

<label class="label flex flex-col {style}">
	<span class="form-label">{label}</span>
	{#if error}
		<span class="pl-1 text-xs text-error-500">{error}</span>
	{/if}
	<textarea
		class="input-wrapper pb-5"
		style="--webkit-overflow-scrolling: touch;"
		{required}
		{name}
		rows={4}
		bind:value
		on:keydown
		on:focusout
	/>
</label>
