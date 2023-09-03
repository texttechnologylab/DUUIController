<script lang="ts">
	import Example from '$lib/assets/Example.png';
	import Fa from 'svelte-fa';
	import { faCircleNodes, faCopy, faUsers } from '@fortawesome/free-solid-svg-icons';

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
				'DUUI is a lightweight framework for running NLP routines without the requirement of extensive knowledge about computer science and programming.',
			icon: faUsers
		},
		{
			title: 'Scalable',
			content:
				'DUUI guarantees horizontal and vertical via a native Docker Swarm implementation. Docker enables machine-specific resource management. Built to work with Docker images distributed within the Docker Swarm network.',
			icon: faCircleNodes
		}
	];
</script>

<div class="container h-full mx-auto flex justify-center items-center">
	<div class="space-y-32 text-center flex flex-col items-center p-4">
		<div class="flex flex-col items-center text-center xl:mb-32">
			<h1 class="h1 !text-5xl max-w-[20ch] leading-loose my-16 md:my-32">
				Reproducible NLP-Analysis automated by <strong class="text-primary-400">DUUI</strong>
			</h1>

			<div class="flex gap-4">
				<a href="/pipelines/new" class="btn variant-filled-primary"> Build a pipeline now </a>
				<a href="/docs" class="btn variant-outline-primary"> Read Docs </a>
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
		<section class="card p-4 pt-8 variant-glass-secondary space-y-16">
			<h2 class="h2">Build NLP Pipelines</h2>
			<img src={Example} alt="" />
		</section>
	</div>
</div>
