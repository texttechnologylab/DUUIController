<!-- @component Unused Component. Use this as a horizontal scroll container.-->
<script lang="ts">
	import { faArrowLeft, faArrowRight } from '@fortawesome/free-solid-svg-icons'
	import Fa from 'svelte-fa'

	let elemCarousel: HTMLDivElement
	export let index: number = 0
	export let images: string[] = []

	const carouselLeft = () => {
		const x =
			elemCarousel.scrollLeft === 0
				? elemCarousel.clientWidth * elemCarousel.childElementCount // loop
				: elemCarousel.scrollLeft - elemCarousel.clientWidth // step left
		elemCarousel.scroll(x, 0)
		elemCarousel.scrollLeft === 0 ? (index = images.length - 1) : (index -= 1)
	}

	const carouselRight = () => {
		const x =
			elemCarousel.scrollLeft === elemCarousel.scrollWidth - elemCarousel.clientWidth
				? 0 // loop
				: elemCarousel.scrollLeft + elemCarousel.clientWidth // step right
		elemCarousel.scroll(x, 0)
		elemCarousel.scrollLeft === elemCarousel.scrollWidth - elemCarousel.clientWidth
			? (index = 0)
			: (index += 1)
	}

	const carouselThumbnail = (i: number) => {
		elemCarousel.scroll(elemCarousel.clientWidth * i, 0)

		index = i
	}
</script>

<div class="space-y-8">
	<div class="grid grid-cols-[auto_1fr_auto] gap-4 items-center">
		<!-- Button: Left -->
		<button
			type="button"
			class="button-neutral !aspect-square !rounded-full !p-3"
			on:click={carouselLeft}
		>
			<Fa icon={faArrowLeft} />
		</button>
		<!-- Full Images -->
		<div bind:this={elemCarousel} class="snap-x snap-mandatory scroll-smooth flex overflow-x-auto">
			{#each images as image}
				<img
					class="snap-center w-[1024px] rounded-container-token"
					src={image}
					alt=""
					loading="lazy"
				/>
			{/each}
		</div>
		<!-- Button: Right -->
		<button
			type="button"
			class="button-neutral !aspect-square !rounded-full !p-3"
			on:click={carouselRight}
		>
			<Fa icon={faArrowRight} />
		</button>
	</div>
	<div class="flex-center-4 justify-center">
		{#each images as id, i}
			<button
				class="w-2 h-2 {i === index
					? 'bg-surface-400-500-token'
					: 'bg-surface-200-700-token'} rounded-full hover:bg-surface-400-500-token"
				on:click={() => carouselThumbnail(i)}
			/>
		{/each}
	</div>
</div>

<!-- Preview 
<div class="hidden md:grid card p-4 grid-cols-6 gap-4">
	{#each unsplashIds as unsplashId, i}
		<button type="button" on:click={() => carouselThumbnail(i)}>
			<img
				class="rounded-container-token"
				src="https://source.unsplash.com/{unsplashId}/256x256"
				alt={unsplashId}
				loading="lazy"
			/>
		</button>
	{/each}
</div>-->
