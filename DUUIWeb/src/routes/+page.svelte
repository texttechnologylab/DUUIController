<script lang="ts">
	import {
		faArrowRight,
		faArrowUpRightFromSquare,
		faArrowsAlt,
		faCheck,
		faCloud,
		faRecycle,
		faUserGroup
	} from '@fortawesome/free-solid-svg-icons'
	import Feature from '$lib/components/Feature.svelte'
	
	import Hosting from '$lib/assets/undraw_cloud_hosting_7xb1.svg'
	import Futuristic from '$lib/assets/undraw_futuristic_interface_re_0cm6.svg'
	import Reminder from '$lib/assets/undraw_reminder_re_fe15.svg'

	import Fa from 'svelte-fa'
	import HLine from '$lib/svelte/widgets/navigation/HLine.svelte'
	import { faAmazon, faDropbox } from '@fortawesome/free-brands-svg-icons'
	import { userSession } from '$lib/store'
	import { scrollIntoView } from '$lib/duui/utils/ui'
	import { onMount } from 'svelte'

	onMount(() => {
		scrollIntoView('top')
	})
	
</script>

<svelte:head>
	<title>DUUI</title>
</svelte:head>

<main class="container-full-h flex justify-center items-center" id="top">
	<div
		class="text-center flex flex-col items-center bg-surface-200/30 dark:bg-surface-500/80 isolate"
	>
		<div
			class="sm:grid grid-cols-2 sm:text-left gap-4 p-8 md:my-12 container relative items-start max-w-7xl"
		>
			<div class="space-y-8 md:space-y-16 2xl:my-8 grid justify-center sm:justify-start">
				<div class="space-y-4">
					<p class="text-surface-400 dark:text-surface-200 text-lg md:text-2xl">
						Natural Language Processing
					</p>
					<h1 class="text-3xl md:text-5xl font-bold !my-8 max-w-[20ch]">
						Accessiblity through Simplicity
					</h1>
					<p class="max-w-[45ch] text-surface-400 dark:text-white md:text-lg">
						<span class="font-bold text-primary-500">Docker Unified UIMA Interface</span>
						- A scalable, flexible, lightweight and feature rich Framework for automated and distributed
						analysis of large text corpora.
					</p>
				</div>

				<div class="flex flex-col sm:flex-row gap-4 items-center">
					{#if $userSession === undefined}
						<a href="/account/register" class="flex items-center gap-2 px-6 py-3 grow-from-left">
							<span> Get Started </span>
							<Fa icon={faArrowRight} size="lg" />
						</a>
					{:else}
						<a href="/pipelinesr" class="flex items-center gap-2 px-6 py-3 grow-from-left">
							<span> Get back to it </span>
							<Fa icon={faArrowRight} size="lg" />
						</a>
					{/if}
				</div>
			</div>

			<div class="h-full relative">
				<img
					class="hidden lg:block absolute max-h-full translate-x-1/2 scale-75 xl:scale-100 2xl:translate-x-3/4 translate-y-1/3"
					src={Hosting}
					alt=""
				/>
				<img
					class="hidden lg:block absolute max-h-full -translate-x-1/4 2xl:translate-x-0 scale-75 xl:scale-100"
					src={Futuristic}
					alt=""
				/>
				<img
					class="hidden sm:block absolute max-h-full lg:translate-x-full 2xl:translate-x-[160%] lg:-translate-y-[20%] scale-75 sm:translate-x-1/4 md:translate-x-0"
					src={Reminder}
					alt=""
				/>
			</div>
		</div>

		<section
			class="bg-white dark:bg-surface-700 w-screen border-y-8 dark:border-surface-900 lg:py-8"
		>
			<div class="container mx-auto p-4 md:py-8 gap-4 grid md:grid-cols-3 items-start max-w-7xl">
				<Feature
					icon={faUserGroup}
					title="Accessible"
					content="DUUI is a lightweight framework for running NLP routines. No extensive knowledge about computer science and programming is required."
				/>
				<div class="md:hidden">
					<HLine thickness={4} width="w-3/4 mx-auto" />
				</div>
				<Feature
					icon={faRecycle}
					title="Reproducible"
					content="Each pipeline component is fully serializable and annotates each processed document. All performed annotations, including analysis engines, models and settings can be fully reconstructed."
				/>
				<div class="md:hidden">
					<HLine thickness={4} width="w-3/4 mx-auto" />
				</div>

				<Feature
					icon={faArrowsAlt}
					title="Scalable"
					content="DUUI guarantees horizontal and vertical via a native Docker Swarm implementation. Docker enables machine-specific resource management."
				/>
			</div>
		</section>
		<div class=" w-screen relative overflow-hidden">
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
							class="flex gap-4 items-center text-lg p-4 px-8 grow-from-left"
						>
							<Fa size="lg" icon={faDropbox} />
							<p>Dropbox</p>

							<Fa icon={faArrowUpRightFromSquare} class="ml-auto" />
						</a>
						<a
							href="https://aws.amazon.com"
							target="_blank"
							class="flex gap-4 items-center text-lg p-4 px-8 grow-from-left"
						>
							<Fa size="lg" icon={faAmazon} />
							<p>AWS</p>

							<Fa icon={faArrowUpRightFromSquare} class="ml-auto" />
						</a>

						<a
							href="https://www.microsoft.com/de-de/microsoft-365/onedrive/online-cloud-storage"
							target="_blank"
							class="flex gap-4 items-center text-lg p-4 px-8 grow-from-left"
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
	</div>
</main>
