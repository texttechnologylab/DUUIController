<script lang="ts">
	import {
		faArrowUpRightFromSquare,
		faArrowsAlt,
		faBookOpen,
		faCheck,
		faCloud,
		faRecycle,
		faRocket,
		faUserGroup
	} from '@fortawesome/free-solid-svg-icons'
	import Card from '$lib/components/containers/Card.svelte'
	import Anchor from '$lib/svelte/widgets/action/Anchor.svelte'

	import Hosting from '$lib/assets/undraw_cloud_hosting_7xb1.svg'
	import Futuristic from '$lib/assets/undraw_futuristic_interface_re_0cm6.svg'
	import Reminder from '$lib/assets/undraw_reminder_re_fe15.svg'

	import Fa from 'svelte-fa'
	import HLine from '$lib/svelte/widgets/navigation/HLine.svelte'
	import { faAmazon, faDropbox } from '@fortawesome/free-brands-svg-icons'
</script>

<svelte:head>
	<title>DUUI</title>
</svelte:head>

<div class="container h-full mx-auto flex justify-center items-center">
	<div
		class="text-center flex flex-col items-center bg-surface-200/30 dark:bg-surface-500/80 isolate"
	>
		<div
			class="sm:grid grid-cols-2 sm:text-left gap-4 p-8 md:my-12 container relative items-start max-w-7xl"
		>
			<div class="space-y-8 md:space-y-16 grid justify-center sm:justify-start">
				<div class="space-y-4">
					<p class="text-surface-400 dark:text-surface-200 text-lg md:text-2xl">
						Natural Language Processing
					</p>
					<h1 class="text-3xl md:text-5xl font-bold !my-8 max-w-[20ch]">
						Accessiblity through Simplicity
					</h1>
					<p class="max-w-[40ch] text-surface-400 dark:text-white md:text-lg">
						<span class="font-bold text-primary-500">DUUI</span>
						- A scalable, flexible, lightweight and feature rich Framework for automated and distributed
						analysis of large text corpora.
					</p>
				</div>

				<div class="flex flex-col sm:flex-row gap-4 items-center">
					<Anchor
						href="/pipelines/editor"
						icon={faRocket}
						text="Get started"
						variant="variant-filled-primary"
					/>
					<a href="/documentation" class="btn hover:variant-filled-primary variant-ringed-primary">
						<Fa icon={faBookOpen} />
						<span>Documentation</span>
					</a>
				</div>
			</div>

			<div class="h-full relative">
				<img
					class="hidden lg:block absolute max-h-full translate-x-1/2 scale-75 xl:scale-100 2xl:translate-x-3/4 translate-y-1/3"
					src={Hosting}
					alt=""
				/>
				<img class="hidden lg:block absolute max-h-full -translate-x-1/4 2xl:translate-x-0 scale-75 xl:scale-100" src={Futuristic} alt="" />
				<img
					class="hidden sm:block absolute max-h-full lg:translate-x-full 2xl:translate-x-[150%] lg:-translate-y-[30%] scale-75 sm:translate-x-1/4 md:translate-x-0"
					src={Reminder}
					alt=""
				/>
			</div>
		</div>

		<section
			class="bg-white dark:bg-surface-700 w-screen border-y-8 dark:border-surface-900 lg:py-8"
		>
			<div class="container mx-auto p-4 md:py-8 gap-4 grid md:grid-cols-3 items-start max-w-7xl">
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
		<div
			class=" w-screen relative overflow-hidden after:-z-10 after:absolute after:inset-0 after:scale-x-125 after:bg-surface-200/50 after:-rotate-[20deg] after:hidden md:after:visible"
		>
			<div class="container max-w-7xl mx-auto space-y-8 my-8">
				<section
					class="flex flex-col md:flex-row gap-4 p-4 md:py-8 items-center text-left justify-between mx-auto"
				>
					<div class="space-y-4 p-4">
						<h2 class="text-2xl font-bold">Integrated Cloud Storage</h2>
						<p class="max-w-[50ch] text-surface-400 dark:text-surface-200">
							Lorem ipsum dolor sit amet consectetur adipisicing elit. Perspiciatis, vero molestias
							aliquam quae impedit nostrum, iusto itaque excepturi voluptatibus molestiae sed eos
							dolor, illo nihil dolore voluptates minus quaerat quia!
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
				<!-- Virtualization -->
				<HLine width="w-1/2 mx-auto" thickness={2} />

				<section
					class="flex flex-col-reverse md:flex-row gap-4 p-4 md:py-8 items-center text-left justify-between mx-auto"
				>
					<div class="space-y-4 p-4 justify-center">
						<ul class="space-y-4">
							<li class="flex gap-4 items-center">
								<Fa icon={faCheck} size="2x" class="text-primary-500" />
								<div class=" gap-4 items-center">
									<p class="font-bold">Vertical Scaling</p>
									<p class="text-surface-400 dark:text-surface-200">
										The Docker deamon enables machine-specific resource management.
									</p>
								</div>
							</li>
							<li class="flex gap-4 items-center">
								<Fa icon={faCheck} size="2x" class="text-primary-500" />
								<div class=" gap-4 items-center">
									<p class="font-bold">Horizontal Scaling</p>
									<p class="text-surface-400 dark:text-surface-200">
										DUUI utilizes Kubernetes and the Docker Swarm Network
									</p>
								</div>
							</li>
						</ul>
					</div>
					<div class="space-y-4 p-4">
						<h2 class="text-2xl font-bold">Scalability through Containers</h2>
						<p class="max-w-[50ch] text-surface-400 dark:text-surface-200">
							Lorem ipsum dolor sit amet consectetur adipisicing elit. Perspiciatis, vero molestias
							aliquam quae impedit nostrum, iusto itaque excepturi voluptatibus molestiae sed eos
							dolor, illo nihil dolore voluptates minus quaerat quia!
						</p>
					</div>
				</section>
			</div>
		</div>

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
