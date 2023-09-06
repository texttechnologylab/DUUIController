<script lang="ts">
	import Pipeline from '$lib/assets/Pipeline.png'
	import Fa from 'svelte-fa'
	import { faArrowTrendUp, faCircleNodes, faCopy, faUsers } from '@fortawesome/free-solid-svg-icons'
	import { faAmazon, faDropbox, faGoogleDrive } from '@fortawesome/free-brands-svg-icons'
	import Card from '$lib/components/containers/Card.svelte'
	import Feature from '$lib/components/containers/Feature.svelte'
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
</script>

<div class="container h-full mx-auto flex justify-center items-center">
	<div
		class="space-y-16 lg:space-y-32 2xl:space-y-64 text-center flex flex-col items-center p-4 max-w-6xl"
	>
		<div class="flex flex-col items-center text-center mb-32 lg:mb-0">
			<h1 class="h1 !text-5xl max-w-[20ch] leading-loose my-16">
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
		<div class="space-y-8 self-stretch">
			<Feature title="Automated NLP Routines" content="Build and manage pipelines in the Browser.">
				<svelte:fragment slot="illustration">
					<img src={Pipeline} alt="" />
				</svelte:fragment></Feature
			>
			<Feature
				reverse={true}
				title="Integrated Storage"
				content="DUUI provides a set of Storage Systems so your data is available and secure."
			>
				<svelte:fragment slot="illustration">
					<ul class="grid grid-cols-3 items-center gap-4 p-4 justify-center text-left">
						<li class="flex flex-col gap-2 justify-center items-center">
							<span><Fa size="2x" icon={faAmazon} /></span>
							<span class="text-sm">AmazonS3</span>
						</li>
						<li class="flex flex-col gap-2 justify-center items-center">
							<span><Fa size="2x" icon={faDropbox} /></span>
							<span class="text-sm">Dropbox</span>
						</li>
						<li class="flex flex-col gap-2 justify-center items-center">
							<span><Fa size="2x" icon={faGoogleDrive} /></span>
							<span class="text-sm">Google Drive</span>
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
</div>
