<script lang="ts">
	import Feature from '$lib/svelte/components/Feature.svelte'
	import {
		faArrowsAlt,
		faBook,
		faCheck,
		faChevronRight,
		faCloud,
		faGlobe,
		faPlus,
		faRecycle,
		faRefresh,
		faUserGroup
	} from '@fortawesome/free-solid-svg-icons'

	import { goto } from '$app/navigation'
	import Logo from '$lib/assets/Logo.svg'
	import Mobile_Dark from '$lib/assets/Screenshots/Mobile Dark.png'
	import Mobile from '$lib/assets/Screenshots/Mobile.png'
	import Processes_Dark from '$lib/assets/Screenshots/Processes Dark.png'
	import Processes from '$lib/assets/Screenshots/Processes.png'
	import { userSession } from '$lib/store'
	import Link from '$lib/svelte/components/Link.svelte'
	import { faAmazon, faDropbox, faGithub, faXTwitter } from '@fortawesome/free-brands-svg-icons'
	import Fa from 'svelte-fa'

	/**
	 * Logout the user and redirect to the login page.
	 */
	const logout = async () => {
		const response = await fetch('/account/logout', { method: 'PUT' })
		if (response.ok) {
			userSession.set(undefined)

			goto('/account/login')
		} else {
			console.error(response.status, response.statusText)
		}
	}
</script>

<svelte:head>
	<title>DUUI</title>
</svelte:head>

<main class="bg-surface-50-900-token" id="top">
	<div class="container mx-auto text-center flex flex-col items-center isolate">
		<div class="w-screen bg-surface-50-900-token top">
			<div class="text-center">
				<div class="space-y-8">
					<!-- Hero -->
					<div class="flex flex-col items-center space-y-8 md:space-y-16 py-8 md:p-12">
						<div class="space-y-4 p-4">
							<p class="text-lg md:text-xl mx-auto dimmed">Docker Unified UIMA Interface</p>
							<h1 class="text-5xl md:text-7xl font-bold">One platform. Unlimited Tools.</h1>
							<p class="max-w-[60ch] mx-auto md:text-lg font-medium dimmed">
								A scalable, flexible, lightweight and feature rich NLP framework for automated and
								distributed analysis of large text corpora.
							</p>
						</div>

						<div class="grid md:grid-cols-2 gap-4">
							{#if $userSession === undefined}
								<a
									href="/account/register"
									class="cta button-primary box-shadow !text-xl !justify-center button-modal shadow-md"
								>
									<span> Get Started </span>
									<Fa icon={faChevronRight} />
								</a>
							{:else}
								<a
									href="/pipelines"
									class="cta button-primary box-shadow !justify-center button-modal shadow-md"
								>
									<span> Get back to it </span>
									<Fa icon={faChevronRight} />
								</a>
							{/if}
							<a
								href="/documentation"
								target="_blank"
								class="cta !hidden md:!inline-flex text-primary-700-200-token shadow-md
								 button-modal hover:variant-soft-primary transition-colors button bordered-soft"
							>
								<span> Documentation </span>
								<Fa icon={faBook} />
							</a>
						</div>
					</div>

					<div class="grid gap-8 justify-center pb-8 mx-auto">
						<div class="section-wrapper">
							<div
								class="flex gap-16 p-4 px-16 items-center rounded-md bg-surface-50-900-token dark:bg-surface-200-700-token"
							>
								<Fa icon={faCheck} size="2x" />
								<p class="md:h4">Catch attention</p>
							</div>
						</div>

						<div class="section-wrapper">
							<div
								class="flex gap-16 p-4 px-16 items-center bg-surface-50-900-token dark:bg-surface-200-700-token"
							>
								{#if $userSession}
									<Fa icon={faCheck} size="2x" />
									<p class="md:h4">Converted to User</p>
								{:else}
									<Fa icon={faRefresh} spin size="2x" />
									<p class="md:h4">Convert to User</p>
								{/if}
							</div>
						</div>

						<div>
							<a
								href="#features"
								class="anchor-neutral bg-surface-50-800-token !aspect-square !rounded-full"
							>
								<Fa icon={faPlus} />
							</a>
						</div>
					</div>
				</div>
			</div>
		</div>

		<section class="w-screen py-16 space-y-16" id="features">
			<div
				class="bottom variant-filled-primary mx-auto grid md:grid-cols-2 xl:grid-cols-3 items-start isolate min-h-[400px] divide-y divide-x xl:divide-y-0 divider"
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
					classes="md:col-span-2 xl:col-span-1"
				/>
				<!-- <div class="col-start-2">
					<Feature icon={faChartSimple} title="Monitor" content="" />
				</div> -->
			</div>
			<div class="p-4 relative container mx-auto text-center text-lg py-8 space-y-16">
				<div class="flex gap-4 flex-col">
					<h3 class="h2">Monitoring</h3>
					<div class="flex justify-center items-center text-center">
						<p>
							Processes are monitored on a Document level including metrics for performed
							annotations and durations for each step. Stay informed on how your pipelines are doing
							even on mobile. Quick access to both metrics and controls in one place.
						</p>
					</div>
				</div>
				<img
					class="block dark:hidden rounded-md bordered-soft shadow-md max-w-[200%] md:max-w-full"
					src={Processes}
					alt=""
				/>
				<img
					class="hidden dark:block rounded-md bordered-soft shadow-md max-w-[200%] md:max-w-full"
					src={Processes_Dark}
					alt=""
				/>
				<div class="absolute top-1/2 md:top-1/3 xl:top-[15%] -right-1/2 md:right-0">
					<img
						class="block dark:hidden bordered-soft shadow-md rounded-md 2xl:max-w-none max-w-[50%] xl:max-w-[75%]"
						src={Mobile}
						alt=""
					/>
					<img
						class="hidden dark:block bordered-soft shadow-md rounded-md 2xl:max-w-none max-w-[50%] xl:max-w-[75%]"
						src={Mobile_Dark}
						alt=""
					/>
				</div>
			</div>
			<div class="container mx-auto py-16 space-y-16 gap-4 text-center text-lg">
				<div class="flex gap-4 flex-col">
					<h3 class="h2">UIMA conform</h3>
					<p class="grow mx-auto">
						All DUUI based annotators are UIMA conform, allowing for the integration of a large
						number of existing and newly created tools. DUUI bundles the diversity of UIMA
						annotators in one framework.
					</p>
				</div>
			</div>
		</section>
		<div
			class="bg-surface-100-800-token w-screen relative overflow-hidden z-50 py-32 md:py-48 bottom-divide"
		>
			<div class="container mx-auto gap-8 my-8 grid md:grid-cols-2 justify-center items-start">
				<!-- Cloud -->
				<section class="flex flex-col items-center justify-center gap-4">
					<h2 class="h2">Storage</h2>
					<p class="dimmed">
						DUUI has built in support for connections to multiple cloud providers like Dropbox,
						Amazon Web Services (AWS) and NextCloud.
					</p>
					<div class="space-y-8 p-4 grid my-4">
						<a
							href="https://www.dropbox.com"
							target="_blank"
							class="cta button-primary box-shadow !text-xl !justify-start"
						>
							<Fa size="lg" icon={faDropbox} />
							<p>Dropbox</p>
						</a>
						<a
							href="https://aws.amazon.com"
							target="_blank"
							class="cta button-primary box-shadow !text-xl !justify-start"
						>
							<Fa size="lg" icon={faAmazon} />
							<p>AWS</p>
						</a>

						<a
							href="https://nextcloud.com/de/"
							target="_blank"
							class="cta button-primary box-shadow !text-xl !justify-start"
						>
							<Fa size="lg" icon={faCloud} />
							<p>NextCloud</p>
							<span class="variant-filled badge hidden md:inline">Development</span>
						</a>
					</div>
				</section>

				<!-- Virtualization -->
				<section class="flex flex-col items-center justify-center gap-4">
					<h2 class="h2">Virtualization</h2>
					<p class="dimmed">Docker and Kubernetes are essential parts of DUUI.</p>
					<div class="space-y-4 p-4 justify-center text-start">
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
				</section>
			</div>
		</div>
		<div class="w-screen variant-filled-primary top">
			<div class="flex flex-col justify-center items-center p-4 py-32 gap-16 bg-fancy">
				<h2 class="h2">Automate big data analysis with DUUI</h2>
				<section class="grid md:grid-cols-2 gap-8">
					<a
						class="cta uppercase box-shadow font-bold button bg-surface-50-900-token text-surface-900-50-token !justify-center box"
						href="/account/register"
					>
						<span>Create Account</span>
						<Fa icon={faChevronRight} />
					</a>
					<a
						class="cta uppercase box-shadow font-bold button bg-surface-50-900-token text-surface-900-50-token !justify-center"
						target="_blank"
						href="https://github.com/texttechnologylab"
					>
						<span>GitHub</span>
						<Fa icon={faGithub} />
					</a>
				</section>
			</div>
		</div>
	</div>
</main>

<footer class="bottom">
	<div class="bg-white dark:bg-black pt-16">
		<div class="grid gap-16 justify-center py-16 container mx-auto p-4">
			<div class="space-y-4 md:my-0 md:border-none">
				<div class="flex flex-col md:flex-row justify-center space-y-8">
					<div class="grid grid-cols-1 gap-4 place-items-center">
						<img src={Logo} class="max-h-8" alt="" />
						<p class="dimmed">Lightweight NLP Framework</p>
					</div>
				</div>
				<div class="flex flex-col md:flex-row justify-center items-center md:items-start gap-4">
					<div class="flex items-center gap-8 0">
						<a
							target="_blank"
							href="https://github.com/texttechnologylab"
							class="transition-300 hover:text-primary-500"
						>
							<Fa icon={faGithub} size="2x" />
						</a>
						<a
							target="_blank"
							href="https://twitter.com/ttlab_ffm"
							class="transition-300 hover:text-primary-500"
						>
							<Fa icon={faXTwitter} size="2x" />
						</a>
						<a
							target="_blank"
							href="https://www.texttechnologylab.org/"
							class="transition-300 hover:text-primary-500"
						>
							<Fa icon={faGlobe} size="2x" />
						</a>
					</div>
				</div>
			</div>
			<hr class="hr" />

			<div
				class="flex flex-col md:flex-row gap-8 md:gap-16 justify-between
					   text-center md:text-left"
			>
				<div class="flex flex-col gap-2 justify-center items-center">
					<p class="text-surface-700-200-token font-bold h4 md:mb-4">Pipelines</p>
					<Link href="/pipelines" dimmed={true}>Dashboard</Link>
					<Link href="/pipelines/build" dimmed={true}>Builder</Link>
				</div>
				<div class="flex flex-col gap-2 justify-center items-center">
					<p class="text-surface-700-200-token font-bold h4 md:mb-4">Documentation</p>
					<Link href="/documentation" dimmed={true}>Framework</Link>
					<Link href="/documentation/api" dimmed={true}>API Reference</Link>
				</div>
				<div class="flex flex-col gap-2 justify-center items-center">
					<p class="text-surface-700-200-token font-bold h4 md:mb-4">Account</p>
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

<style>
	.top {
		clip-path: polygon(0 0, 100% 0, 100% calc(100% - 50px), 50% 100%, 0 calc(100% - 50px));
	}
	.bottom {
		clip-path: polygon(0 0, 50% 50px, 100% 0, 100% 100%, 0 100%);
		margin-top: -50px;
	}

	@media (min-width: 600px) {
		.top {
			clip-path: polygon(0 0, 100% 0, 100% calc(100% - 70px), 50% 100%, 0 calc(100% - 70px));
		}
		.bottom {
			clip-path: polygon(0 0, 50% 70px, 100% 0, 100% 100%, 0 100%);
			margin-top: -70px;
		}
	}

	.bottom-divide {
		clip-path: polygon(0 0, 100% 137px, 100% 100%, 0 100%);
		margin-top: -127px;
	}
</style>
