<script lang="ts">
	import {
		faArrowsAltH,
		faBookOpen,
		faDatabase,
		faRecycle,
		faRobot,
		faRocket,
		faUsersLine
	} from '@fortawesome/free-solid-svg-icons'
	import Card from '$lib/components/containers/Card.svelte'
	import Anchor from '$lib/svelte/widgets/action/Anchor.svelte'
	import TimelineV2 from '$lib/svelte/widgets/timeline/TimelineV2.svelte'
	</script>

<div class="dark:bg-surface-700 container h-full mx-auto flex justify-center items-center">
	<div
		class="space-y-16 lg:space-y-32 text-center flex flex-col items-center p-4 container max-w-7xl"
	>
		<div class="flex flex-col items-center text-center lg:mb-0">
			<div class="my-8 lg:my-16 space-y-4">
				<h1 class="h1 !text-5xl max-w-[20ch] leading-loose">
					Natural Language Processing for Everyone
					<!-- Reproducible NLP-Analysis automated by -->
				</h1>
				<!-- <img class="max-h-10 mx-auto" src={Logo} alt="" /> -->
			</div>

			<div class="grid md:grid-cols-2 gap-4 p-4">
				<Anchor
					href="/pipelines/new"
					icon={faRocket}
					text="Get started"
					variant="variant-filled-primary"
				/>
				<Anchor
					href="/documentation"
					icon={faBookOpen}
					text="Documentation"
					variant="variant-ringed-primary"
				/>
			</div>
		</div>
		<section class="grid md:grid-cols-2 lg:grid-cols-3 gap-4 container">
			<Card
				icon={faRecycle}
				title="Reproducible"
				content="Each pipeline component is fully serializable and annotates each processed document. All performed annotations, including analysis engines, models and settings can be fully reconstructed."
				classes="lg:col-span-2"
			/>
			<Card
				icon={faUsersLine}
				title="Accessible"
				content="DUUI is a lightweight framework for running NLP routines. No extensive knowledge about computer science and programming is required."
			/>
			<Card
				icon={faArrowsAltH}
				title="Scalable"
				content="DUUI guarantees horizontal and vertical via a native Docker Swarm implementation. Docker enables machine-specific resource management. Built to work with Docker images distributed within the Docker Swarm network."
			/>
			<Card
				icon={faRobot}
				title="Automated"
				content="Build and manage pipelines in the Browser."
				classes="lg:col-span-2"
			/>
			<Card
				icon={faDatabase}
				title="Storage"
				content="DUUI provides a set of Storage Systems so your data is available and secure."
				classes="lg:col-span-2"
			/>
		</section>
	</div>
</div>
