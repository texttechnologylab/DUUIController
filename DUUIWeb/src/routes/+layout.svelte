<script lang="ts">
	import '../app.postcss'
	import Logo from '$lib/assets/Logo.svg'

	import { AppShell, AppBar, LightSwitch, type ModalComponent } from '@skeletonlabs/skeleton'
	import { Drawer, getDrawerStore } from '@skeletonlabs/skeleton'
	import { Toast, type DrawerSettings } from '@skeletonlabs/skeleton'
	import Fa from 'svelte-fa'
	import {
		faGlobe,
		faBars,
		faLayerGroup,
		faPlus,
		faBookOpen,
		faGears,
		faNetworkWired,
		faMap,
		faArrowRightToBracket,
		faUser,
		faUserPlus,
		faArrowRightFromBracket,
		faLock,
		faLink
	} from '@fortawesome/free-solid-svg-icons'
	import { faGithub } from '@fortawesome/free-brands-svg-icons'

	import { initializeStores } from '@skeletonlabs/skeleton'
	import { goto, onNavigate } from '$app/navigation'
	import { storePopup } from '@skeletonlabs/skeleton'

	import { computePosition, autoUpdate, offset, shift, flip, arrow } from '@floating-ui/dom'
	import ComponentEditor from '$lib/components/ComponentEditor.svelte'
	import { Modal } from '@skeletonlabs/skeleton'
	import Anchor from '$lib/svelte/widgets/action/Anchor.svelte'
	import Sidebar from '$lib/svelte/widgets/navigation/Sidebar.svelte'
	import Menu from '$lib/svelte/widgets/navigation/Menu.svelte'
	import { Api, makeApiCall } from '$lib/utils/api'
	import ActionButton from '$lib/svelte/widgets/action/ActionButton.svelte'
	import DeleteModal from '$lib/svelte/widgets/modal/DeleteModal.svelte'
	import { storage } from '$lib/store'
	import { fly } from 'svelte/transition'

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

	export let data
	let { loggedIn } = data

	$: loggedIn = $storage.session !== ''

	const logout = async () => {
		const response = await makeApiCall(Api.Logout, 'PUT', {})
		if (response.ok) {
			loggedIn = false
			goto('/account/auth/login?logout=true')
		}
	}

	const modalRegistry: Record<string, ModalComponent> = {
		deleteModal: { ref: DeleteModal }
	}
</script>

<Modal components={modalRegistry} />
<Toast position="br" />
<Drawer rounded="rounded-none">
	{#if $drawerStore.id === 'sidebar'}
		<Sidebar {loggedIn} />
	{:else if $drawerStore.id === 'component'}
		<ComponentEditor component={$drawerStore.meta.component} />
	{/if}
</Drawer>
<!-- App Shell -->
<AppShell class="dark:bg-surface-700">
	<svelte:fragment slot="header">
		<!-- App Bar -->
		<AppBar shadow="shadow-lg dark:bg-surface-800" padding="p-4">
			<svelte:fragment slot="lead">
				<div class="flex items-center">
					<button class="btn-icon md:hidden" on:click={() => drawerStore.open(sidebarDrawer)}>
						<Fa icon={faBars} size="lg" />
					</button>
					<a href="/">
						<img src={Logo} alt="The letters DUUI" class="hidden md:block max-h-8" />
					</a>
				</div>
			</svelte:fragment>
			<svelte:fragment slot="trail">
				<div class="hidden md:flex items-center gap-4">
					<Menu
						background="bg-primary-hover-token p-2"
						label="Pipelines"
						offset={24}
						placement="bottom-end"
					>
						<svelte:fragment slot="title">
							<span>Pipelines</span>
						</svelte:fragment>
						<svelte:fragment slot="content">
							<div
								class="shadow-lg border-[1px] bg-white dark:bg-surface-600 border-surface-400/20"
							>
								<div class="p-4 space-y-4">
									<p class="font-bold">Manage</p>
									<div class="flex flex-col text-left gap-2">
										<a
											href="/pipelines"
											class="transition-colors hover:text-primary-500 text-left rounded-none text-sm"
											>Dashboard</a
										>
										<a
											href="/pipelines/new"
											class="transition-colors hover:text-primary-500 text-left rounded-none text-sm"
											>Create</a
										>
										<a
											href="/pipelines/statistics"
											class="transition-colors hover:text-primary-500 text-left rounded-none text-sm"
											>Statistics</a
										>
									</div>
								</div>
							</div>
						</svelte:fragment>
					</Menu>
					<Menu
						background="bg-primary-hover-token p-2"
						label="Documentation"
						offset={24}
						placement="bottom-end"
					>
						<svelte:fragment slot="title">
							<span>Documentation</span>
						</svelte:fragment>
						<svelte:fragment slot="content">
							<div
								class="grid grid-cols-2 gap-8 shadow-lg border-[1px] bg-white dark:bg-surface-600 border-surface-400/20"
							>
								<div class="p-4 space-y-4">
									<p class="font-bold">Framework</p>
									<div class="flex flex-col text-left gap-2">
										<a
											href="/documentation#introduction"
											class="transition-colors hover:text-primary-500 text-left rounded-none text-sm"
											>Introduction</a
										>
										<a
											href="/documentation#composer"
											class="transition-colors hover:text-primary-500 text-left rounded-none text-sm"
											>Composer</a
										>

										<a
											href="/documentation#driver"
											class="transition-colors hover:text-primary-500 text-left rounded-none text-sm"
											>Driver</a
										>

										<a
											href="/documentation#component"
											class="transition-colors hover:text-primary-500 text-left rounded-none text-sm"
											>Component</a
										>
										<a
											href="/documentation#document"
											class="transition-colors hover:text-primary-500 text-left rounded-none text-sm"
											>Document</a
										>
										<a
											href="/documentation#process"
											class="transition-colors hover:text-primary-500 text-left rounded-none text-sm"
											>Process</a
										>
									</div>
								</div>
								<div class="p-4 space-y-4">
									<p class="font-bold">API</p>
									<div class="flex flex-col text-left gap-2">
										<a
											href="/documentation/api#web"
											class="transition-colors hover:text-primary-500 text-left rounded-none text-sm"
											>Web</a
										>

										<a
											href="/documentation/api#java"
											class="transition-colors hover:text-primary-500 text-left rounded-none text-sm"
											>Java</a
										>

										<a
											href="/documentation/api#python"
											class="transition-colors hover:text-primary-500 text-left rounded-none text-sm"
											>Python</a
										>
									</div>
								</div>
							</div>
						</svelte:fragment>
					</Menu>
					<Menu
						background="bg-primary-hover-token p-2"
						label="Account"
						offset={24}
						placement="bottom-end"
					>
						<svelte:fragment slot="title">
							<span>Account</span>
						</svelte:fragment>
						<svelte:fragment slot="content">
							<div
								class="grid grid-cols-2 shadow-lg border-[1px] bg-white dark:bg-surface-600 border-surface-400/20"
							>
								{#if loggedIn}
									<div class="p-4 space-y-4">
										<p class="font-bold">User</p>
										<div class="flex flex-col text-left gap-2">
											<a
												href="/account/user/profile"
												class="transition-colors hover:text-primary-500 text-left rounded-none text-sm"
												>Profile</a
											>
											<a
												href="/account/user/profile"
												class="transition-colors hover:text-primary-500 text-left rounded-none text-sm"
												>Connections</a
											>
											<a
												href="/account/user/profile"
												class="transition-colors hover:text-primary-500 text-left rounded-none text-sm"
												>Security</a
											>
										</div>
									</div>
								{/if}

								<div class="p-4 space-y-4">
									<p class="font-bold">Authentication</p>
									<div class="flex flex-col text-left gap-2">
										{#if !loggedIn}
											<a
												href="/account/auth/login"
												class="transition-colors hover:text-primary-500 text-left rounded-none text-sm"
												>Login</a
											>
											<a
												href="/account/auth/login?register=true"
												class="transition-colors hover:text-primary-500 text-left rounded-none text-sm"
												>Register</a
											>
										{:else}
											<a
												on:click={logout}
												href="/account/auth/login"
												class="transition-colors hover:text-primary-500 text-left rounded-none text-sm"
												>Logout</a
											>
										{/if}
									</div>
								</div>
							</div>
						</svelte:fragment>

						<svelte:fragment>
							<div
								class="flex flex-col shadow-lg border-[1px] bg-white dark:bg-surface-600 border-surface-400/20"
							>
								{#if !loggedIn}
									<Anchor
										href="/account/auth/login"
										icon={faArrowRightToBracket}
										text="Login"
										rounded="rounded-md"
										_class="justify-start gap-8 bg-primary-hover-token"
										variant=""
									/>
									<Anchor
										href="/account/auth/login?register=true"
										icon={faUserPlus}
										text="Register"
										_class="justify-start gap-8 bg-primary-hover-token"
										variant=""
									/>
								{:else}
									<Anchor
										href="/account/user/profile"
										icon={faUser}
										text="Profile"
										_class="justify-start gap-8 bg-primary-hover-token"
										variant=""
									/>
									<Anchor
										href="/account/user/connections"
										icon={faLink}
										text="Connections"
										_class="justify-start gap-8 bg-primary-hover-token"
										variant=""
									/>
									<Anchor
										href="/account/user/security"
										icon={faLock}
										text="Security"
										_class="justify-start gap-8 bg-primary-hover-token"
										variant=""
									/>
									<ActionButton
										on:click={logout}
										icon={faArrowRightFromBracket}
										text="Logout"
										_class="justify-start gap-8 bg-primary-hover-token"
										variant=""
									/>
								{/if}
							</div>
						</svelte:fragment>
					</Menu>
				</div>
				<a href="/">
					<img src={Logo} alt="The letters DUUI" class="md:hidden block max-h-8 pr-4" />
				</a>
				<LightSwitch class="md:block hidden" />
			</svelte:fragment>
		</AppBar>
	</svelte:fragment>

	<!-- Page Route Content -->

	<slot />

	<svelte:fragment slot="pageFooter">
		<footer id="page-footer" class="flex-none">
			<div
				class="page-footer bg-surface-50 dark:bg-surface-700 border-t border-surface-400/20 text-xs md:text-base"
			>
				<div class="w-full max-w-7xl mx-auto space-y-4 py-8">
					<div class="flex flex-col md:flex-row justify-center space-y-8">
						<div class="grid grid-cols-1 gap-4 place-items-center">
							<img src={Logo} class="max-h-8" alt="" />
							<p class="!text-sm opacity-80">Lightweight NLP Framework</p>
						</div>
					</div>
					<div
						class="flex flex-col md:flex-row justify-center items-center md:items-start space-y-4 md:space-y-0"
					>
						<div class="flex space-x-4">
							<Anchor
								icon={faGithub}
								text="Github"
								href="https://github.com/texttechnologylab/DUUIController"
								target="_blank"
								rel="noreferrer"
							/>
							<Anchor
								icon={faGlobe}
								text="TTLab"
								href="https://www.texttechnologylab.org/"
								target="_blank"
								rel="noreferrer"
							/>
						</div>
					</div>
				</div>
			</div>
		</footer>
	</svelte:fragment>
</AppShell>
