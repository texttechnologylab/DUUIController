<!--
	@component
	A component representing a Likert Scale.
-->
<script lang="ts">
	import { feedbackStore } from '$lib/store'
	import { RadioGroup, RadioItem } from '@skeletonlabs/skeleton'
	import TextArea from './TextArea.svelte'

	export let question: string = 'Do you like this Component?'
	export let name: string

	export let wrapper: string = 'space-y-8 section-wrapper p-4 py-8 md:p-16 text-center text-lg'
	export let reason: boolean = false
	export let score: number
	export let min: number = 1
	export let max: number = 7
	export let labelLeft: string =
		$feedbackStore.language === 'english' ? 'Strongly disagree' : 'Stimme nicht zu'
	export let labelRight: string =
		$feedbackStore.language === 'english' ? 'Strongly agree' : 'Stimme zu'

	const options = Array.from({ length: max }, (_, index) => min + index)
</script>

<div class={wrapper}>
	<p class="max-w-[40ch] mx-auto text-lg">{question}</p>
	<input type="number" bind:value={score} class="sr-only" {name} />
	<hr class="hr" />
	<div class="flex flex-col gap-4">
		<div class="flex justify-between items-center text-sm">
			{#if labelLeft}
				<p class="max-w-[8ch]">{labelLeft}</p>
			{/if}
			{#if labelRight}
				<p class="max-w-[8ch]">{labelRight}</p>
			{/if}
		</div>
		<RadioGroup
			class="section-wrapper items-center"
			active="variant-filled-primary"
			rounded="!rounded-full"
		>
			{#each options as option}
				<RadioItem
					class="aspect-square md:aspect-auto flex items-center justify-center "
					name={'' + option}
					value={option}
					bind:group={score}
				>
					{option}
				</RadioItem>
			{/each}
		</RadioGroup>
	</div>

	{#if (score === min || score === max) && reason}
		<TextArea
			label=""
			name={name + '-reason'}
			placeholder={$feedbackStore.language === 'english'
				? 'Why did you chose this rating?'
				: 'Warum hast du dich für diese Bewertung entschieden?'}
		/>
	{/if}
</div>
