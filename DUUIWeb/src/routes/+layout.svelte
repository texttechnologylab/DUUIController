<script lang="ts">
	import '../app.css'
	import Logo from '$lib/assets/Logo.svg'

	import { AppShell, AppBar, LightSwitch, type ModalComponent } from '@skeletonlabs/skeleton'
	import { Drawer, getDrawerStore } from '@skeletonlabs/skeleton'
	import { Toast, type DrawerSettings } from '@skeletonlabs/skeleton'
	import Fa from 'svelte-fa'
	import { faGlobe, faBars, faArrowRightFromBracket } from '@fortawesome/free-solid-svg-icons'
	import { faGithub } from '@fortawesome/free-brands-svg-icons'

	import { initializeStores } from '@skeletonlabs/skeleton'
	import { afterNavigate, beforeNavigate, goto, onNavigate } from '$app/navigation'
	import { storePopup } from '@skeletonlabs/skeleton'

	import { computePosition, autoUpdate, offset, shift, flip, arrow } from '@floating-ui/dom'
	import { Modal } from '@skeletonlabs/skeleton'
	import Sidebar from '$lib/svelte/widgets/navigation/Sidebar.svelte'
	import { Api, makeApiCall } from '$lib/duui/utils/api'
	import DeleteModal from '$lib/svelte/widgets/modal/DeleteModal.svelte'
	import { userSession } from '$lib/store'
	import Link from '$lib/components/Link.svelte'
	import DocumentModal from '$lib/svelte/widgets/modal/DocumentModal.svelte'

	export let data
	let { user } = data
	$userSession = user

	initializeStores()

	const drawerStore = getDrawerStore()
	const sidebarDrawer: DrawerSettings = {
		id: 'sidebar',
		width: 'w-full sm:w-[360px]',
		rounded: 'rounded-none'
	}

	storePopup.set({ computePosition, autoUpdate, offset, shift, flip, arrow })

	onNavigate(() => {
		drawerStore.close()
	})

	beforeNavigate(() => {
		let expirationDate = $userSession?.expires ? new Date($userSession.expires) : undefined

		if (expirationDate && expirationDate < new Date()) {
			console.log('Login session expired.')
			$userSession = null
		}
	})

	const logout = async () => {
		const response = await makeApiCall(Api.Logout, 'PUT', {})
		if (response.ok) {
			loggedIn = false
			userSession.set(undefined)

			goto('/account/login')
		} else {
			console.error(response.status, response.statusText)
		}
	}

	$: loggedIn = !!$userSession?.session

	const modalRegistry: Record<string, ModalComponent> = {
		deleteModal: { ref: DeleteModal },
		documentModal: { ref: DocumentModal }
	}
</script>

<Modal components={modalRegistry} />
<Toast position="br" />
<Drawer rounded="rounded-none">
	{#if $drawerStore.id === 'sidebar'}
		<Sidebar {loggedIn} />
	{/if}
</Drawer>
<!-- App Shell -->
<AppShell class="dark:bg-surface-700">
	<svelte:fragment slot="header">
		<!-- App Bar -->
		<AppBar shadow="shadow-lg dark:bg-surface-900">
			<svelte:fragment slot="lead">
				<div class="flex items-center gap-4">
					<button class="btn-icon lg:hidden" on:click={() => drawerStore.open(sidebarDrawer)}>
						<Fa icon={faBars} size="lg" />
					</button>
					<a href="/">
						<img src={Logo} alt="The letters DUUI" class="hidden lg:block max-h-8" />
					</a>
					{#if $userSession?.role === 'admin'}
						<span class="badge variant-soft-tertiary font-bold">ADMIN</span>
					{/if}
				</div>
			</svelte:fragment>
			<svelte:fragment slot="trail">
				<div class="hidden lg:flex items-center gap-8 font-heading-token">
					<Link href="/pipelines">Pipelines</Link>
					<Link href="/pipelines/editor">Editor</Link>
					<Link href="/documentation">Framework</Link>
					<Link href="/documentation/api">API Reference</Link>

					{#if $userSession}
						<Link href="/account">Account</Link>

						<button
							class="p-0 btn inline-flex items-center hover:text-primary-500 transition-colors"
							on:click={logout}
						>
							<span>Logout</span>
							<Fa icon={faArrowRightFromBracket} />
						</button>
					{:else}
						<Link href="/account/login">Login</Link>
						<Link href="/account/register">Register</Link>
					{/if}
				</div>
				<a href="/">
					<img src={Logo} alt="The letters DUUI" class="md:hidden block max-h-8 pr-4" />
				</a>
				<LightSwitch class="md:block hidden" rounded="rounded-full" />
			</svelte:fragment>
		</AppBar>
	</svelte:fragment>

	<!-- Page Route Content -->

	<slot />

	<svelte:fragment slot="pageFooter">
		<footer>
			<div class="border-t border-surface-400/20 bg-white dark:bg-surface-900/50">
				<div
					class="relative
				 md:after:visible after:invisible after:absolute after:w-[2px] after:h-full after:scale-y-[80%] after:bg-surface-400/20 after:left-1/2 after:top-0 after:rounded-full
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
							<div class="flex gap-4">
								<a
									href="https://github.com/texttechnologylab/DUUIController"
									target="_blank"
									rel="noreferrer"
									class="variant-ringed-primary hover:variant-filled-primary flex items-center justify-center gap-2 btn"
								>
									<span>Github</span>
									<Fa icon={faGithub} />
								</a>
								<a
									href="https://www.texttechnologylab.org/"
									target="_blank"
									rel="noreferrer"
									class="variant-ringed-primary hover:variant-filled-primary flex items-center justify-center gap-2 btn"
								>
									<span>TTLab</span>
									<Fa icon={faGlobe} />
								</a>
							</div>
						</div>
					</div>

					<div
						class="flex flex-col md:flex-row gap-8 md:gap-16 justify-between text-base
							   text-center md:text-left"
					>
						<div class="flex flex-col gap-2">
							<p class="text-black dark:text-surface-100 font-bold md:mb-4">Pipelines</p>
							<Link href="/pipelines" dimmed={true}>Dashboard</Link>
							<Link href="/pipelines/editor" dimmed={true}>Editor</Link>
						</div>
						<div class="flex flex-col gap-2">
							<p class="text-black dark:text-surface-100 font-bold md:mb-4">Documentation</p>
							<Link href="/documentation" dimmed={true}>Framework</Link>
							<Link href="/documentation/api" dimmed={true}>API Reference</Link>
						</div>
						<div class="flex flex-col gap-2">
							<p class="text-black dark:text-surface-100 font-bold md:mb-4">Account</p>
							{#if $userSession}
								<Link href="/account" dimmed={true}>Account</Link>

								<button
									class="inline md:text-start hover:text-primary-500 transition-colors
									text-surface-400 dark:text-surface-200"
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
	</svelte:fragment>
</AppShell>
