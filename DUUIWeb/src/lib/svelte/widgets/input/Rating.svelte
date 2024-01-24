<script lang="ts">
	import TextInput from './TextInput.svelte'

	export let value: number = 1
	export let max: number = 7
	export let labelLeft: string = 'Strongly Disagree'
	export let labelRight: string = 'Strongly Agree'
	export let question: string = 'Do you like this Component?'
	export let name: string
</script>

<div class="space-y-4 section-wrapper p-4 md:p-16 text-center self-stretch">
	<p class="max-w-[40ch] mx-auto md:pb-8">{question}</p>

	<div
		class="grid grid-cols-7 items-center justify-center section-wrapper col-span-2 border border-color"
	>
		<input type="number" bind:value class="appearance-none hidden" {name} />
		{#each Array.from({ length: max }, (_, index) => index + 1) as rating}
			<button
				class="border-r border-surface-200 dark:border-surface-500 transition-colors p-2 px-4
					inline-flex justify-center items-center last-of-type:border-r-0
					 {value === rating ? 'variant-filled-primary' : 'hover:variant-soft-primary'}"
				on:click|preventDefault={() => (value = rating)}
			>
				{rating}
			</button>
		{/each}
	</div>
	<div class="flex justify-between items-center">
		{#if labelLeft}
			<p>{labelLeft}</p>
		{/if}
		{#if labelRight}
			<p>{labelRight}</p>
		{/if}
	</div>
	{#if value === 1}
		<TextInput placeholder="Why did you chose this rating?" name={name + '-why'} />
	{/if}
</div>
