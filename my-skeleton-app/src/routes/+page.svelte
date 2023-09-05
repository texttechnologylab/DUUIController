<script lang="ts">
	import Example from '$lib/assets/Example.png'
	import Fa from 'svelte-fa'
	import { faArrowTrendUp, faCircleNodes, faCopy, faUsers } from '@fortawesome/free-solid-svg-icons'
	import { faAmazon, faDropbox, faGoogleDrive } from '@fortawesome/free-brands-svg-icons'
	import Card from '$lib/components/containers/Card.svelte'
	import Feature from '$lib/components/containers/Feature.svelte'
	import { popup, type PopupSettings } from '@skeletonlabs/skeleton'
	const sections = [
		{
			title: 'Reproducible',
			content:
				'Each pipeline component is fully serializable and annotates each processed document. All performed annotations, including analysis engines, models and settings can be fully reconstructed.',
			icon: faCopy
		},
		{
			title: 'Accessible',
			content:
				'DUUI is a lightweight framework for running NLP routines. No extensive knowledge about computer science and programming is required.',
			icon: faUsers
		},
		{
			title: 'Scalable',
			content:
				'DUUI guarantees horizontal and vertical via a native Docker Swarm implementation. Docker enables machine-specific resource management. Built to work with Docker images distributed within the Docker Swarm network.',
			icon: faCircleNodes
		}
	]

	const popupFeatured: PopupSettings = {
		// Represents the type of event that opens/closed the popup
		event: 'click',
		// Matches the data-popup value on your popup element
		target: 'popupFeatured',
		// Defines which side of your trigger the popup will appear
		placement: 'bottom'
	}
	
</script>

<div class="container h-full mx-auto flex justify-center items-center">
	<button class="btn variant-filled" use:popup={popupFeatured}>Show Popup</button>

	<div class="card p-4 w-72 shadow-xl" data-popup="popupFeatured">
		<div><p>Demo Content</p></div>
		<div class="arrow bg-surface-100-800-token" />
	</div>
	<div
		class="space-y-16 lg:space-y-32 2xl:space-y-64 text-center flex flex-col items-center p-4 max-w-6xl"
	>
		<div class="flex flex-col items-center text-center">
			<h1 class="h1 !text-5xl max-w-[20ch] leading-loose my-24">
				Reproducible NLP-Analysis automated by <strong class="text-primary-400">DUUI</strong>
			</h1>

			<div class="flex gap-4">
				<a href="/pipelines/new" class="btn variant-filled-primary flex items-center">
					<span>Get Started</span>
					<span><Fa icon={faArrowTrendUp} /></span>
				</a>
				<a href="/docs" class="btn variant-ghost-surface"> Read Docs </a>
			</div>
		</div>
		<section class="grid grid-cols-1 lg:grid-cols-3 gap-4">
			{#each sections as { title, content, icon }}
				<Card {title} {content} {icon} />
			{/each}
		</section>
		<Feature title="Automated NLP Routines" content="Build and manage pipelines in the Browser." />
		<Feature
			title="Integrated Storage"
			content="DUUI provides a set of Storage Systems so your data is available and secure."
		>
			<svelte:fragment slot="illustration">
				<ul class="flex gap-4 p-4 justify-start">
					<li class="flex flex-col gap-2 items-center">
						<span><Fa size="2x" icon={faAmazon} /></span>
						<span class="text-left">AmazonS3</span>
					</li>
					<li class="flex flex-col gap-2 items-center">
						<span><Fa size="2x" icon={faDropbox} /></span>
						<span class="text-left">Dropbox</span>
					</li>
					<li class="flex flex-col gap-2 items-center">
						<span><Fa size="2x" icon={faGoogleDrive} /></span>
						<span class="text-left">Google Drive</span>
					</li>
				</ul>
			</svelte:fragment>
		</Feature>
		<Feature
			title="Headline placeholder"
			content={'Lorem ipsum dolor sit amet consectetur adipisicing elit. Suscipit enim excepturi laboriosam,' +
				'numquam explicabo facilis error autem fugit est repellendus eius consequatur voluptatibus' +
				'architecto voluptatem quae repellat accusamus impedit dolores vitae dolor quis cum' +
				'quos.'}
		/>
	</div>
</div>
