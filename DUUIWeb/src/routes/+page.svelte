<script lang="ts">
	import {
		faArrowUpRightDots,
		faArrowUpRightFromSquare,
		faArrowsAlt,
		faBookOpen,
		faCloud,
		faFile,
		faFileArchive,
		faFolder,
		faLink,
		faRecycle,
		faRocket,
		faUserGroup
	} from '@fortawesome/free-solid-svg-icons'
	import Card from '$lib/components/containers/Card.svelte'
	import Anchor from '$lib/svelte/widgets/action/Anchor.svelte'

	import ImageOverview from '$lib/assets/Screenshots/hero.png'
	import ImageOverviewDark from '$lib/assets/Screenshots/overview-dark.png'
	import ImageTimeline from '$lib/assets/Screenshots/Timeline.png'
	import ImageTimelineDark from '$lib/assets/Screenshots/timeline-dark.png'
	import ImageEditor from '$lib/assets/Screenshots/editor.png'
	import ImageEditorDark from '$lib/assets/Screenshots/editor-dark.png'

	import Fa from 'svelte-fa'
	import HLine from '$lib/svelte/widgets/navigation/HLine.svelte'
	import { faAmazon, faDropbox } from '@fortawesome/free-brands-svg-icons'
</script>

<div class="container h-full mx-auto flex justify-center items-center">
	<div
		class="text-center flex flex-col items-center w-screen bg-surface-200/30 dark:bg-surface-500/80 isolate"
	>
		<div class="lg:flex md:text-left gap-4 p-8 md:my-24 container relative items-start">
			<div class="space-y-8 md:space-y-16 grid">
				<div class="space-y-4">
					<p class="text-surface-400 dark:text-surface-200 text-lg md:text-2xl">
						Natural Language Processing
					</p>
					<h1 class="text-3xl md:text-5xl font-bold !my-8">Accessiblity through Simplicity</h1>
					<p class="max-w-[40ch] text-surface-600 dark:text-white md:text-lg">
						<span class="font-bold text-primary-500">DUUI</span>
						- Ein skalierbares, flexibles, leichtgewichtiges und funktionsreiches Framework zur automatischen
						und verteilten Analyse von Textkorpora.
					</p>
				</div>

				<div class="flex flex-col md:flex-row gap-4 items-center">
					<Anchor
						href="/pipelines/editor"
						icon={faRocket}
						text="Get started"
						variant="variant-filled-primary"
					/>
					<a
						href="/documentation"
						class="btn hover:variant-filled-primary rounded-none variant-ringed-primary"
					>
						<Fa icon={faBookOpen} />
						<span>Documentation</span>
					</a>
				</div>
			</div>

			<div class="justify-center items-center hidden lg:flex lg:pl-32 relative">
				<img src={ImageOverview} alt="" class="dark:hidden block object-cover" />
				<img src={ImageOverviewDark} alt="" class="hidden dark:block object-cover" />
			</div>
		</div>

		<section class="bg-white dark:bg-surface-700 w-screen border-y-8 dark:border-surface-900">
			<div class="container mx-auto p-4 md:py-8 gap-4 grid md:grid-cols-3 items-start">
				<Card
					icon={faUserGroup}
					title="Accessible"
					content="DUUI is a lightweight framework for running NLP routines. No extensive knowledge about computer science and programming is required."
				/>
				<div class="md:hidden">
					<HLine thickness={4} width="w-3/4 mx-auto" />
				</div>
				<Card
					icon={faRecycle}
					title="Reproducible"
					content="Each pipeline component is fully serializable and annotates each processed document. All performed annotations, including analysis engines, models and settings can be fully reconstructed."
				/>
				<div class="md:hidden">
					<HLine thickness={4} width="w-3/4 mx-auto" />
				</div>

				<Card
					icon={faArrowsAlt}
					title="Scalable"
					content="DUUI guarantees horizontal and vertical via a native Docker Swarm implementation. Docker enables machine-specific resource management."
				/>
			</div>
		</section>
		<section
			class="md:flex gap-4 p-4 md:py-8 lg:my-16 container text-center md:text-left justify-center"
		>
			<div class="space-y-4 p-4">
				<h2 class="text-2xl font-bold">Storage</h2>
				<p class="max-w-[50ch]">
					Lorem ipsum dolor sit amet consectetur adipisicing elit. Perspiciatis, vero molestias
					aliquam quae impedit nostrum, iusto itaque excepturi voluptatibus molestiae sed eos dolor,
					illo nihil dolore voluptates minus quaerat quia!
				</p>
			</div>
			<div class="space-y-4 text-white p-4 justify-start">
				<a
					href="https://www.dropbox.com"
					target="_blank"
					class="flex gap-4 items-center font-bold text-lg bg-primary-500 p-4 px-8 rounded-full shadow-lg
					relative isolate overflow-hidden group
					after:absolute after:inset-0 after:bg-primary-700 after:-z-10 after:rounded-full after:scale-x-0
					hover:after:scale-x-100 after:transition-transform after:origin-left after:duration-500"
				>
					<Fa size="lg" icon={faDropbox} />
					<p>Dropbox</p>

					<Fa icon={faArrowUpRightFromSquare} class="ml-auto" />
				</a>
				<a
					href="https://aws.amazon.com"
					target="_blank"
					class="flex gap-4 items-center font-bold text-lg bg-primary-500 p-4 px-8 rounded-full shadow-lg
					relative isolate overflow-hidden group
					after:absolute after:inset-0 after:bg-primary-700 after:-z-10 after:rounded-full after:scale-x-0
					hover:after:scale-x-100 after:transition-transform after:origin-left after:duration-500"
				>
					<Fa size="lg" icon={faAmazon} />
					<p>AWS</p>

					<Fa icon={faArrowUpRightFromSquare} class="ml-auto" />
				</a>

				<a
					href="https://www.microsoft.com/de-de/microsoft-365/onedrive/online-cloud-storage"
					target="_blank"
					class="flex gap-4 items-center font-bold text-lg bg-primary-500 p-4 px-8 rounded-full shadow-lg
					relative isolate overflow-hidden group
					after:absolute after:inset-0 after:bg-primary-700 after:-z-10 after:rounded-full after:scale-x-0
					hover:after:scale-x-100 after:transition-transform after:origin-left after:duration-500"
				>
					<Fa size="lg" icon={faCloud} />
					<p>OneDrive</p>
					<span class="variant-filled badge hidden md:inline">Development</span>

					<Fa icon={faArrowUpRightFromSquare} class="ml-auto" />
				</a>
			</div>
		</section>
		<!-- 
		<section class="grid md:grid-cols-2 lg:grid-cols-3 gap-4 container">
			<Card
				icon={faRobot}
				title="Automated"
				content="Build and manage pipelines in the Browser."
				classes="lg:col-span-2"
			/>
			<Card
				icon={faDatabase}
				title="Storage"
				content="Connect to one of the Cloud Storage Solutions available for DUUI."
				classes="lg:col-span-2"
			/>
		</section> -->
	</div>
</div>
