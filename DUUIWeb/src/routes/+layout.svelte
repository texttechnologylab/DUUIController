<script lang="ts">
	import '../app.postcss'
	import Logo from '$lib/assets/Logo.svg'
	import Icon from '$lib/assets/favicon.svg'

	import { AppShell, AppBar, LightSwitch } from '@skeletonlabs/skeleton'
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
		faArrowRightFromBracket
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

	const logout = async () => {
		const response = await makeApiCall(Api.Logout, 'PUT', {})
		if (response.ok) {
			loggedIn = false
			goto('/account/login')
		}
	}
</script>

<Modal />
<Toast />
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
						background="bg-primary-hover-token p-2 rounded-md"
						name="Pipelines"
						offset={32}
						placement="bottom-end"
					>
						<svelte:fragment slot="title">
							<span>Pipelines</span>
						</svelte:fragment>
						<svelte:fragment slot="content">
							<div
								class="flex flex-col text-left dark:bg-surface-600 dark:text-on-surface-token bg-surface-100 shadow-lg text-surface-800 p-4"
							>
								<Anchor
									href="/pipelines"
									icon={faLayerGroup}
									text="Dashboard"
									rounded="rounded-md"
									_class="justify-start gap-8 bg-primary-hover-token"
									variant=""
								/>
								<Anchor
									href="/pipelines/new"
									icon={faPlus}
									text="Builder"
									rounded="rounded-md"
									_class="justify-start gap-8 bg-primary-hover-token"
									variant=""
								/>
							</div>
						</svelte:fragment>
					</Menu>
					<Menu
						background="bg-primary-hover-token p-2 rounded-md"
						name="Documentation"
						offset={32}
						placement="bottom-end"
					>
						<svelte:fragment slot="title">
							<span>Documentation</span>
						</svelte:fragment>
						<svelte:fragment slot="content">
							<div
								class="flex flex-col text-left dark:bg-surface-600 dark:text-on-surface-token bg-surface-100 shadow-lg text-surface-800 p-4"
							>
								<Anchor
									href="/documentation#quick-start"
									icon={faBookOpen}
									text="Quick Start"
									rounded="rounded-md"
									_class="justify-start gap-8 bg-primary-hover-token"
									variant=""
								/>
								<Anchor
									href="/documentation#composer"
									icon={faGears}
									text="Composer"
									rounded="rounded-md"
									_class="justify-start gap-8 bg-primary-hover-token"
									variant=""
								/>
								<Anchor
									href="/documentation#driver"
									icon={faNetworkWired}
									text="Driver"
									rounded="rounded-md"
									_class="justify-start gap-8 bg-primary-hover-token"
									variant=""
								/>
								<Anchor
									href="/documentation#component"
									icon={faMap}
									text="Component"
									rounded="rounded-md"
									_class="justify-start gap-8 bg-primary-hover-token"
									variant=""
								/>
							</div>
						</svelte:fragment>
					</Menu>
					<Menu
						background="bg-primary-hover-token p-2 rounded-md"
						name="Account"
						offset={32}
						placement="bottom-end"
					>
						<svelte:fragment slot="title">
							<span>Account</span>
						</svelte:fragment>
						<svelte:fragment slot="content">
							<div
								class="flex flex-col text-left dark:bg-surface-600 dark:text-on-surface-token bg-surface-100 shadow-lg text-surface-800 p-4"
							>
								<Anchor
									href="/account"
									icon={faUser}
									text="Profile"
									rounded="rounded-md"
									_class="justify-start gap-8 bg-primary-hover-token"
									variant=""
								/>
								{#if !loggedIn}
									<Anchor
										href="/account/login"
										icon={faArrowRightToBracket}
										text="Login"
										rounded="rounded-md"
										_class="justify-start gap-8 bg-primary-hover-token"
										variant=""
									/>
									<Anchor
										href="/account/register"
										icon={faUserPlus}
										text="Register"
										rounded="rounded-md"
										_class="justify-start gap-8 bg-primary-hover-token"
										variant=""
									/>
								{:else}
									<ActionButton
										on:click={logout}
										icon={faArrowRightFromBracket}
										text="Logout"
										rounded="rounded-md"
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
				class="page-footer bg-surface-50 dark:bg-surface-700 border-t border-surface-500 text-xs md:text-base"
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
