<script>
	import LinkIcon from '../../components/Buttons/IconButton.svelte';
</script>

<svelte:head>
	<title>About</title>
	<meta name="description" content="About DUUI" />
</svelte:head>

<article class="about p-4 max-w-6xl mx-auto">
	<h1 class="page-title text-center text-4xl mt-8 mb-16 text-gray-800">
		DockerUnifiedUIMAInterface
	</h1>
	<p class="intro leading-8 text-lg text-gray-600 my-16">
		Automatic analysis of large text corpora is a complex task. This complexity particularly
		concerns the question of time efficiency. Furthermore, efficient, flexible, and extensible
		textanalysis requires the continuous integration of every new text analysis tools. Since there
		are currently, in the area of NLP and especially in the application context of UIMA, only very
		few to no adequate frameworks for these purposes, which are not simultaneously outdated or can
		no longer be used for security reasons, this work will present a new approach to fill this gap.
		To this end, we present Docker Unified UIMA Interface (DUUI), a scalable, flexible, lightweight,
		and featurerich framework for automated and distributed analysis of text corpora that leverages
		experience in Big Data analytics and virtualization with Docker.
	</p>
	<div class="wrapper grid grid-cols-2 gap-8">
		<div class="border-2 text-slate-800 border-slate-800 p-8">
			<div class="header flex justify-between items-center">
				<h2 class="column-title font-bold text-2xl">DUUI App</h2>
				<LinkIcon iconName="github-solid" />
			</div>
		</div>
		<div class="border-2 text-slate-800 border-slate-800 p-8">
			<div class="header flex justify-between items-center">
				<h2 class="column-title font-bold text-2xl">DUUI Web</h2>
				<LinkIcon
					href="https://github.com/texttechnologylab/DUUIController"
					iconName="github-solid"
				/>
			</div>
		</div>
	</div>
	<div class="wrapper license" />
</article>
