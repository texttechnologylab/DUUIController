<script lang="ts">
	export let label: string = ''
	export let name: string
	export let value: number = 0

	export let min: number = 0
	export let max: number = 100
	export let step: number = 1

	export let required: boolean = false
	export let showRange: boolean = false
	export let style: string = ''
</script>

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
		
		{max}
		{step}
		{name}
		bind:value
		{required}
	/>
</label>
