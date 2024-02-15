<script lang="ts">
	import Feature from '$lib/svelte/components/Feature.svelte'
	import {
		faArrowUpRightFromSquare,
		faArrowsAlt,
		faBook,
		faChartSimple,
		faCheck,
		faChevronRight,
		faCloud,
		faGlobe,
		faRecycle,
		faUserGroup
	} from '@fortawesome/free-solid-svg-icons'

	import { goto } from '$app/navigation'
	import Logo from '$lib/assets/Logo.svg'
	import { userSession } from '$lib/store'
	import Link from '$lib/svelte/components/Link.svelte'
	import { faAmazon, faDropbox, faGithub, faXTwitter } from '@fortawesome/free-brands-svg-icons'
	import Fa from 'svelte-fa'
	import Document from '$lib/assets/Screenshots/Document_with_Download.png'
	import Pipeline from '$lib/assets/Screenshots/Pipeline_Processes.png'

	const logout = async () => {
		const response = await fetch('/account/logout', { method: 'PUT' })
		if (response.ok) {
			userSession.set(undefined)

			goto('/account/login')
		} else {
			console.error(response.status, response.statusText)
		}
	}

	// onMount(() => {
	// 	scrollIntoView('top')
	// })
</script>

<svelte:head>
	<title>DUUI</title>
</svelte:head>

<main class="container mx-auto flex justify-center items-center" id="top">
	<div
		class="text-center flex flex-col items-center bg-surface-200/30 dark:bg-surface-500/80 isolate"
	>
		<div class="text-center p-8 md:my-12 self-stretch">
			<div class="space-y-8 md:space-y-16 2xl:my-8 flex flex-col items-center">
				<div class="space-y-2">
					<p class="text-lg md:text-xl py-4 mx-auto">Natural Language Processing</p>
					<h1 class="text-3xl md:text-6xl font-bold !mb-8">Docker Unified UIMA Interface</h1>
					<p class="max-w-[60ch] mx-auto dimmed md:text-lg font-medium">
						A scalable, flexible, lightweight and feature rich NLP framework for automated and
						distributed analysis of large text corpora.
					</p>
				</div>

				<div class="grid md:grid-cols-2 gap-4">
					{#if $userSession === undefined}
						<a href="/account/register" class="button-modal button-primary shadow-md">
							<span> Get Started </span>
							<Fa icon={faChevronRight} />
						</a>
					{:else}
						<a href="/pipelines" class="button-modal button-primary">
							<span> Get back to it </span>
							<Fa icon={faChevronRight} />
						</a>
					{/if}
					<a
						href="/documentation"
						class="text-primary-700-200-token shadow-md button-modal hover:variant-soft-primary transition-colors button bordered-soft"
					>
						<span> Documentation </span>
						<Fa icon={faBook} />
					</a>
				</div>
			</div>
		</div>

		<section class="bg-surface-50-900-token w-screen p-4 border-y border-color">
			<div
				class="container mx-auto py-16 gap-4 grid md:grid-cols-2 xl:grid-cols-3 items-start isolate"
			>
				<Feature
					icon={faUserGroup}
					title="Accessible"
					content="DUUI is a lightweight framework for running NLP routines. No extensive knowledge about computer science and programming is required."
				/>
				<Feature
					icon={faRecycle}
					title="Reproducible"
					content="Each pipeline component is fully serializable and annotates each processed document. All performed annotations, including analysis engines, models and settings can be fully reconstructed."
				/>
				<Feature
					icon={faArrowsAlt}
					title="Scalable"
					content="DUUI guarantees horizontal and vertical via a native Docker Swarm implementation. Docker enables machine-specific resource management."
				/>
				<!-- <div class="col-start-2">
					<Feature icon={faChartSimple} title="Monitor" content="" />
				</div> -->
			</div>
			<div class="container mx-auto text-start p-4 py-8 space-y-8">
				<div class=" grid md:grid-cols-2 items-center justify-center md:gap-16 gap-4">
					<div class="space-y-2">
						<h3 class="h3">Monitoring</h3>
						<p>
							Pipelines are monitored in detail on a Document level including metrics for performed
							annotations and durations for each step in a process.
						</p>
					</div>
					<img src={Document} alt="" />
				</div>
				
				<div class=" grid md:grid-cols-2 items-center justify-center md:gap-16 gap-4">
					<img src={Pipeline} alt="" />
					<div class="space-y-2">
						<h3 class="h3">Monitoring</h3>
						<p>
							Pipelines are monitored in detail on a Document level including metrics for performed
							annotations and durations for each step in a process.
						</p>
					</div>
				</div>
			</div>
		</section>
		<div class="w-screen relative overflow-hidden">
			<div class="container max-w-7xl mx-auto space-y-8 my-8">
				<section
					class="flex flex-col md:flex-row gap-4 p-4 md:py-8 items-center text-left justify-between mx-auto"
				>
					<div class="space-y-4 p-4">
						<h2 class="text-2xl font-bold">Integrated Cloud Storage</h2>
						<p class="text-surface-500 dark:text-surface-200">
							DUUI has built in support for connections to multiple cloud providers like Dropbox,
							Amazon Web Services (AWS) and OneDrive.
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
				<hr class="hr" />

				<section
					class="flex flex-col-reverse md:flex-row gap-4 p-4 md:py-8 items-center text-left justify-between mx-auto"
				>
					<div class="space-y-4 p-4 justify-center">
						<ul class="space-y-4">
							<li class="flex gap-4 items-center">
								<Fa icon={faCheck} size="2x" class="text-primary-500" />
								<div class=" gap-4 items-center">
									<p class="font-bold">Vertical Scaling</p>
									<p class="text-surface-500 dark:text-surface-200">
										The Docker deamon enables machine-specific resource management.
									</p>
								</div>
							</li>
							<li class="flex gap-4 items-center">
								<Fa icon={faCheck} size="2x" class="text-primary-500" />
								<div class=" gap-4 items-center">
									<p class="font-bold">Horizontal Scaling</p>
									<p class="text-surface-500 dark:text-surface-200">
										DUUI utilizes Kubernetes and the Docker Swarm Network
									</p>
								</div>
							</li>
						</ul>
					</div>
					<div class="space-y-4 p-4">
						<h2 class="text-2xl font-bold">Scalability through Containers</h2>
						<p class="max-w-[50ch] text-surface-500 dark:text-surface-200">
							Docker and Kubernetes are essential parts of DUUI.
						</p>
					</div>
				</section>
			</div>
		</div>
		<div class="w-screen variant-filled-primary min-h-[200px]">
			<div class="flex flex-col justify-center items-center py-16 gap-8">
				<h2 class="h2">Automate big data analysis with DUUI</h2>
				<section class="md:p-8 grid md:grid-cols-2 gap-4">
					<button class="button bg-surface-50-900-token text-surface-900-50-token">
						<span>Create Account</span>
						<Fa icon={faChevronRight} />
					</button>
					<button class="button bg-surface-50-900-token text-surface-900-50-token">
						<span>GitHub</span>
						<Fa icon={faGithub} />
					</button>
				</section>
			</div>
		</div>
	</div>
</main>

<footer>
	<div class="border-t border-surface-400/20 bg-surface-50-900-token">
		<div
			class="relative
		 lg:after:visible after:invisible after:absolute after:w-[2px] after:h-full after:scale-y-[80%] after:bg-surface-400/20 after:left-1/2 after:top-0 after:rounded-full
		 flex flex-col md:flex-row gap-4 md:justify-between py-16 max-w-7xl container mx-auto p-4 space-y-16 md:space-y-0"
		>
			<div class="space-y-4 md:my-0 md:border-none">
				<div class="flex flex-col md:flex-row justify-center space-y-8">
					<div class="grid grid-cols-1 gap-4 place-items-center">
						<img src={Logo} class="max-h-8" alt="" />
						<p class="!text-sm">Lightweight NLP Framework</p>
					</div>
				</div>
				<div class="flex flex-col md:flex-row justify-center items-center md:items-start gap-4">
					<div class="flex items-center gap-8 0">
						<a
							target="_blank"
							href="https://github.com/texttechnologylab"
							class="transition-opacity opacity-70 hover:opacity-100"
						>
							<Fa icon={faGithub} size="2x" />
						</a>
						<a
							target="_blank"
							href="https://twitter.com/ttlab_ffm"
							class="transition-opacity opacity-70 hover:opacity-100"
						>
							<Fa icon={faXTwitter} size="2x" />
						</a>
						<a
							target="_blank"
							href="https://www.texttechnologylab.org/"
							class="transition-opacity opacity-70 hover:opacity-100"
						>
							<Fa icon={faGlobe} size="2x" />
						</a>
					</div>
				</div>
			</div>

			<div
				class="flex flex-col md:flex-row gap-8 md:gap-16 justify-between text-base
					   text-center md:text-left"
			>
				<div class="flex flex-col gap-2 justify-center items-center">
					<p class="text-surface-700-200-token font-bold md:mb-4">Pipelines</p>
					<Link href="/pipelines" dimmed={true}>Dashboard</Link>
					<Link href="/pipelines/editor" dimmed={true}>Editor</Link>
				</div>
				<div class="flex flex-col gap-2 justify-center items-center">
					<p class="text-surface-700-200-token font-bold md:mb-4">Documentation</p>
					<Link href="/documentation" dimmed={true}>Framework</Link>
					<Link href="/documentation/api" dimmed={true}>API Reference</Link>
				</div>
				<div class="flex flex-col gap-2 justify-center items-center">
					<p class="text-surface-700-200-token font-bold md:mb-4">Account</p>
					{#if $userSession}
						<Link href="/account" dimmed={true}>Account</Link>

						<button
							class="inline md:text-start hover:text-primary-500 transition-colors
							text-surface-500 dark:text-surface-200 animate-underline"
							on:click={logout}
						>
							Logout
						</button>
					{:else}
						<Link href="/account/login" dimmed={true}>Login</Link>
						<Link href="/account/register" dimmed={true}>Register</Link>
					{/if}
				</div>
			</div>
		</div>
	</div>
</footer>
