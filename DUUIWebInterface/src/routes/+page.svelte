<script>
	import { Icon } from 'flowbite-svelte-icons';
	import { notifications } from '$lib/notifications';
	import Toast from '../components/Toast.svelte';
	import { fade, fly } from 'svelte/transition';
	import { onMount } from 'svelte';

	export let data;
	let { sections } = data;
</script>

<svelte:head>
	<title>About</title>
	<meta name="description" content="About DUUI" />
</svelte:head>
<!-- m-16 max-w-7xl mx-auto -->

<article class="flex flex-col items-center">
	<h1
		class="page-title text-center text-5xl max-w-[30ch] font-bold leading-normal font-heading mt-32 text-gray-800"
	>
		Fast, automated and reproducible <strong class="text-blue-600">NLP-Analysis</strong>
	</h1>
	<p class="mb-64 mt-8 text-2xl font-heading">Straightforward integration of NLP routines</p>

	<div class=" w-full bg-[#f5f5f5]">
		<div class="grid grid-cols-3 max-w-7xl mx-auto gap-16">
			{#each sections as section (section.title)}
				<section class=" flex flex-col items-center gap-6 text-center py-16">
					{#if section.image !== ''}
						<div class="py-4 text-[#555]">
							<Icon name={section.image} class="w-10 h-10" />
						</div>
					{/if}
					<h2 class="font-heading text-3xl">
						{section.title}
					</h2>
					<p class="leading-relaxed font-body max-w-[40ch] text-[#333]">{section.content}</p>
				</section>
			{/each}
		</div>
	</div>

	<div class="max-w-7xl mx-auto my-16">
		<p class="leading-7 text-lg text-[#333]">
			Automatic analysis of large text corpora is a complex task. This complexity particularly
			concerns the question of time efficiency. Furthermore, efficient, flexible, and extensible
			textanalysis requires the continuous integration of every new text analysis tools. Since there
			are currently, in the area of NLP and especially in the application context of UIMA, only very
			few to no adequate frameworks for these purposes, which are not simultaneously outdated or can
			no longer be used for security reasons, this work will present a new approach to fill this
			gap. To this end, we present Docker Unified UIMA Interface (DUUI), a scalable, flexible,
			lightweight, and featurerich framework for automated and distributed analysis of text corpora
			that leverages experience in Big Data analytics and virtualization with Docker.
		</p>
	</div>
	<!-- <button on:click={() => notifications.info('Information', 'Message', 3000)}> info! </button>
	<button on:click={() => notifications.warning('Wait a moment...', 'Message', 3000)}>
		Warn!
	</button>
	<button
		on:click={() => notifications.success('Success', 'Pipeline has been created succesfully', 3000)}
	>
		Succeed!
	</button>
	<button on:click={() => notifications.danger('STOP NOW!', 'Message', 3000)}> Danger! </button> -->
</article>
