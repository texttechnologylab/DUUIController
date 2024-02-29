<script lang="ts">
	import { goto, onNavigate } from '$app/navigation'
	import { page } from '$app/stores'
	import { scrollIntoView } from '$lib/duui/utils/ui'
	import { feedbackStore, isDarkModeStore, userSession } from '$lib/store'
	import Rating from '$lib/svelte/components/Input/Rating.svelte'
	import TextArea from '$lib/svelte/components/Input/TextArea.svelte'
	import { faCheck, faChevronLeft, faChevronRight, faUser } from '@fortawesome/free-solid-svg-icons'
	import { ProgressBar, RadioGroup, RadioItem, SlideToggle } from '@skeletonlabs/skeleton'
	import Dropdown from '$lib/svelte/components/Input/Dropdown.svelte'
	import { onMount } from 'svelte'
	import Fa from 'svelte-fa'
	import { content } from './content'
	import { getPlotOptions } from './charts'

	export let data

	const { feedback } = data
	let success: boolean = ($page.url.searchParams.get('success') || 'false') === 'true'

	$: language = $feedbackStore.language

	let ApexCharts
	let loaded: boolean = false

	onMount(() => {
		$feedbackStore.step = +($page.url.searchParams.get('step') || $feedbackStore.step)
		async function loadApexCharts() {
			const module = await import('apexcharts')
			ApexCharts = module.default
			window.ApexCharts = ApexCharts
			loaded = true
		}

		loadApexCharts()
	})

	let plotOptions
	let duuiFilter: 'Experienced' | 'Inexperienced' | 'All' = 'All'

	$: {
		let filteredFeedback = feedback.filter((item) => {
			if (duuiFilter === 'All') return item
			if (duuiFilter === 'Experienced') return item.duui
			if (duuiFilter === 'Inexperienced') return !item.duui
		})
		plotOptions = getPlotOptions(filteredFeedback, 'Averages ' + duuiFilter, $isDarkModeStore)
	}

	const chart = (node: HTMLDivElement, options: any) => {
		if (!loaded) return

		let _chart = new ApexCharts(node, options)
		_chart.render()

		return {
			update(options: any) {
				_chart.updateOptions(options)
			},
			destroy() {
				_chart.destroy()
			}
		}
	}

	onNavigate(() => {
		scrollIntoView('top')
	})

	const next = () => {
		$feedbackStore.step += 1
		goto(`/feedback?step=${$feedbackStore.step}`)
	}

	const previous = () => {
		$feedbackStore.step -= 1
		goto(`/feedback?step=${$feedbackStore.step}`)
	}

	const finish = async () => {
		const response = await fetch('/api/feedback', {
			method: 'POST',
			body: JSON.stringify($feedbackStore)
		})

		if (response.ok) success = true
	}

	let isStepComplete: boolean = false

	$: {
		if ($feedbackStore.step === 1) isStepComplete = stepOneComplete()
		if ($feedbackStore.step === 2) isStepComplete = stepTwoComplete()
	}

	const stepOneComplete = () => {
		return (
			$feedbackStore.programming > 0 &&
			$feedbackStore.nlp > 0 &&
			($feedbackStore.duuiRating > 0 || !$feedbackStore.duui) &&
			$feedbackStore.java > 0 &&
			$feedbackStore.python > 0
		)
	}

	const stepTwoComplete = () => {
		return (
			$feedbackStore.requirements > 0 &&
			$feedbackStore.frustration > 0 &&
			$feedbackStore.ease > 0 &&
			$feedbackStore.correction > 0
		)
	}

	const reset = () => {
		$feedbackStore = {
			name: '',
			message: '',
			step: 0,
			language: 'english',
			programming: -1,
			java: -1,
			python: -1,
			duui: false,
			duuiRating: -1,
			nlp: -1,
			requirements: -1,
			frustration: -1,
			correction: -1,
			ease: -1
		}

		goto('/account')
	}
</script>

<div class="bg-surface-100-800-token pattern h-full">
	{#if success}
		<div class="h-full flex items-center justify-center p-4">
			<div class="section-wrapper p-8 gap-8 grid justify-center">
				<p class="text-lg">Thank you for your feedback!</p>
				<button class="cta box-shadow button-primary !justify-center" on:click={reset}>
					<Fa icon={faUser} />
					<span>Account</span>
				</button>
			</div>
		</div>
	{:else}
		<div class="relative space-y-8 container mx-auto grid items-center justify-center">
			{#if $feedbackStore.step > 0}
				<div class="sticky w-screen top-0 p-4 bg-surface-100-800-token border-b border-color">
					<ProgressBar
						max={3}
						bind:value={$feedbackStore.step}
						class="section-wrapper max-w-[600px] mx-auto"
						height="h-4"
						rounded="!rounded-md"
						meter="bg-primary-500"
					/>
				</div>
			{/if}

			<div class="container mx-auto max-w-screen-md space-y-8 w-full p-4 bg-surface-100-800-token">
				{#if $feedbackStore.step === 0}
					<div
						class="section-wrapper grid justify-center gap-8 items-center p-4 md:p-12 text-center"
					>
						<RadioGroup
							class="section-wrapper justify-center min-w-[256px] mx-auto"
							active="variant-filled-primary"
							rounded="!rounded-full"
						>
							<RadioItem
								bind:group={$feedbackStore.language}
								name={'english'}
								value={'english'}
								class="md:min-w-[64px]"
							>
								English
							</RadioItem>
							<RadioItem
								bind:group={$feedbackStore.language}
								name={'german'}
								value={'german'}
								class="md:min-w-[64px]"
							>
								Deutsch
							</RadioItem>
						</RadioGroup>
						<hr class="hr" />
						<h2 class="h3">{content.thank.title[language]}</h2>
						<p>
							{content.thank.intro[language]}
						</p>

						<hr class="hr" />

						<button
							class="cta box-shadow button-primary !justify-center mx-auto min-w-[256px]"
							on:click={next}
						>
							<span>Start</span>
							<Fa icon={faChevronRight} />
						</button>
					</div>
				{:else}
					<div class="space-y-8">
						{#if $feedbackStore.step === 1}
							<div class="space-y-8 text-center">
								<h2 class="h2">{content.experience.title[language]}</h2>
								<div class="space-y-4">
									<Rating
										name="programming"
										reason={false}
										bind:score={$feedbackStore.programming}
										question={content.experience.programming[language]}
									/>
									<Rating
										name="nlp"
										reason={false}
										bind:score={$feedbackStore.nlp}
										question={content.experience.nlp[language]}
									/>
									<div
										class="section-wrapper p-8 text-center text-lg flex flex-col items-center justify-center"
									>
										<SlideToggle
											background="bg-surface-100-800-token"
											active="variant-filled-primary"
											rounded="rounded-full"
											border="bordered-soft"
											name="duui"
											bind:checked={$feedbackStore.duui}
											on:change={() => ($feedbackStore.duuiRating = -1)}
										>
											{content.experience.duui[language]}
										</SlideToggle>
										{#if $feedbackStore.duui}
											<Rating
												wrapper="space-y-8 p-4 py-8 md:p-16 text-center text-lg box"
												name="duui_rating"
												reason={false}
												bind:score={$feedbackStore.duuiRating}
												question={content.experience.duuiRating[language]}
											/>
										{/if}
									</div>

									<Rating
										name="java"
										reason={false}
										bind:score={$feedbackStore.java}
										question={content.experience.java[language]}
									/>
									<Rating
										name="python"
										reason={false}
										bind:score={$feedbackStore.python}
										question={content.experience.python[language]}
									/>
								</div>
							</div>
						{:else if $feedbackStore.step === 2}
							<div class="space-y-8 text-center">
								<h2 class="h2">{content.usability.title[language]}</h2>

								<div class="space-y-4">
									<Rating
										name="requirements"
										bind:score={$feedbackStore.requirements}
										question={content.usability.requirements[language]}
									/>
									<Rating
										name="frustration"
										bind:score={$feedbackStore.frustration}
										question={content.usability.frustration[language]}
									/>
									<Rating
										name="ease"
										bind:score={$feedbackStore.ease}
										question={content.usability.ease[language]}
									/>
									<Rating
										name="correction"
										bind:score={$feedbackStore.correction}
										question={content.usability.correction[language]}
									/>
								</div>
							</div>
						{:else if $feedbackStore.step === 3}
							<div class="p-4 space-y-4 max-w-screen-md mx-auto section-wrapper">
								<!-- <TextInput name="name" label="Name" bind:value={$feedbackStore.name} /> -->
								<TextArea
									placeholder="Optional"
									name="message"
									label={$feedbackStore.language === 'english' ? 'Message' : 'Nachricht'}
									bind:value={$feedbackStore.message}
								/>
							</div>
						{/if}
						<div class="flex justify-between items-center gap-4">
							<button
								class="button-neutral !justify-center w-[160px]"
								on:click={previous}
								type="button"
							>
								<Fa icon={faChevronLeft} />
								<span>{content.previous[language]}</span>
							</button>

							{#if $feedbackStore.step === 3}
								<button class="button-neutral !justify-center w-[160px]" on:click={finish}>
									<span>{content.finish[language]}</span>
									<Fa icon={faCheck} />
								</button>
							{:else}
								<button
									type="button"
									class="button-neutral !justify-center w-[160px]"
									on:click={next}
									disabled={!isStepComplete}
								>
									<span>{content.next[language]}</span>
									<Fa icon={faChevronRight} />
								</button>
							{/if}
						</div>
					</div>
				{/if}
			</div>
		</div>
	{/if}
	{#if loaded && feedback.length > 0 && $userSession}
		<div class="p-4 space-y-4 max-w-screen-xl mx-auto section-wrapper m-4">
			<div use:chart={plotOptions} />
			<div class="grid gap-4 items-center max-w-[256px] mx-auto">
				<Dropdown
					bind:value={duuiFilter}
					options={['All', 'Experienced', 'Inexperienced']}
					label="DUUI Experience"
				/>
			</div>
		</div>
	{/if}
</div>

<style>
	.pattern {
		background-image: repeating-linear-gradient(
			45deg,
			#006c9811 0,
			#006c9811 0.5px,
			transparent 0,
			transparent 50%
		);
		background-size: 16px 16px;
	}

	p {
		margin-inline: auto;
		max-width: 60ch;
	}
</style>
