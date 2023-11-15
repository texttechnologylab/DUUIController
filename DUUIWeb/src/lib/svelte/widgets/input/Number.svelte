<script lang="ts">
	export let label: string
	export let name: string
	export let value: number = 0

	export let min: number = 0
	export let max: number = 100

	export let required: boolean = false
	export let showRange: boolean = false
</script>

<label class="label flex flex-col items-start">
	<span class="uppercase text-xs tracking-widest"
		>{label}{showRange ? ` (${min} - ${max})` : ''}</span
	>
	<input
		class="border-[1px] bg-white dark:bg-surface-600 border-surface-400/20
        [appearance:textfield] [&::-webkit-outer-spin-button]:appearance-none [&::-webkit-inner-spin-button]:appearance-none"
		type="number"
		{min}
		{max}
		{name}
		bind:value
		{required}
	/>
</label>
