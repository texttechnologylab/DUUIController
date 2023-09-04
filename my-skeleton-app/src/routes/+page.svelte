<script lang="ts">
	import Example from '$lib/assets/Example.png'
	import Fa from 'svelte-fa'
	import {
		faArrowTrendUp,
		faCircleNodes,
		faCopy,
		faNewspaper,
		faPlus,
		faUsers
	} from '@fortawesome/free-solid-svg-icons'
	import { faAmazon, faDropbox, faGoogleDrive } from '@fortawesome/free-brands-svg-icons'
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
				<div class="card variant-glass p-4 shadow-lg md:p-10 space-y-4 text-center">
					<span class="btn-icon btn-icon-xl variant-glass-primary">
						<Fa {icon} />
					</span>
					<h3 class="h3">{title}</h3>
					<p>{content}</p>
				</div>
			{/each}
		</section>
		<div class="grid lg:grid-cols-2 gap-4">
			<section
				class="flex flex-col justify-start items-start self-stretch gap-8 text-left p-4 variant-glass card"
			>
				<div class="flex flex-col items-start space-y-4">
					<h3 class="h3">Automated NLP Routines</h3>
					<p>Build and manage pipelines in the Browser.</p>
				</div>
				<img class="max-w-[300px] lg:max-w-lg" src={Example} alt="" />
			</section>

			<section class="flex flex-col self-stretch gap-8 text-left p-4 variant-glass card">
				<div class="flex flex-col items-start self-start space-y-4">
					<h3 class="h3">Integrated Storage</h3>
					<p>DUUI provides a set of Storage Systems so your data is available and secure.</p>
				</div>
				<ul class="flex gap-4 p-2 justify-center">
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
			</section>
		</div>
		<section class="card variant-glass p-4 text-left self-stretch gap-4 flex flex-col">
			<h3 class="h3">Headline placeholder</h3>
			<p>
				Lorem ipsum dolor sit amet consectetur adipisicing elit. Suscipit enim excepturi laboriosam,
				numquam explicabo facilis error autem fugit est repellendus eius consequatur voluptatibus
				architecto voluptatem quae repellat accusamus impedit dolores vitae dolor quis cum
				molestias. Ullam excepturi cupiditate eos cumque ab, aliquam itaque impedit, expedita, dolor
				asperiores incidunt possimus! Repellendus accusamus neque totam earum cum, quidem eum
				reprehenderit voluptas blanditiis quaerat vel autem non dolores. Reprehenderit quia soluta
				laudantium asperiores dicta blanditiis ea. Consequuntur vitae aliquid iure culpa repellendus
				quos.
			</p>
		</section>
	</div>
</div>
