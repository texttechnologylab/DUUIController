<script lang="ts">
	import TextInput from './TextInput.svelte'

	export let value: number = 1
	export let max: number = 5
	export let labelLeft: string = 'Disagree'
	export let labelRight: string = 'Agree'
	export let question: string = 'Do you like this Component?'
	export let name: string
</script>

<div class="space-y-4 section-wrapper p-4 text-center">
	<p class="text-lg">{question}</p>
	<div
		class="grid grid-cols-2 gap-2 items-center text-sm
				md:flex md:text-base md:gap-4 justify-center"
	>
		{#if labelLeft}
			<p class="row-start-2">{labelLeft}</p>
		{/if}
		<div class="grid grid-cols-5 items-center section-wrapper col-span-2">
			<input type="number" bind:value class="appearance-none hidden" {name} />
			{#each Array.from({ length: max }, (_, index) => index + 1) as rating}
				<button
					class="border-r border-surface-200 dark:border-surface-500 transition-colors p-2 px-4
					inline-flex justify-center items-center nth
					{max > 5 && rating <= max - 5 ? 'border-b' : ''}
					 {value === rating ? 'variant-filled-primary' : 'hover:variant-soft-primary'}"
					on:click|preventDefault={() => (value = rating)}
				>
					{rating}
				</button>
			{/each}
		</div>
		{#if labelRight}
			<p class="text-end">{labelRight}</p>
		{/if}
	</div>
	{#if value === 1}
		<TextInput placeholder="Why did you chose this rating?" name={name + '-why'} />
	{/if}
</div>

<style>
	.nth:nth-of-type(5n) {
		border-right: none;
	}
</style>
