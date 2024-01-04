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
	import MenuItem from '$lib/svelte/widgets/navigation/MenuItem.svelte'

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
<AppShell class="dark:bg-surface-700 ">
	<svelte:fragment slot="header">
		<!-- App Bar -->
		<AppBar shadow="shadow-lg dark:bg-surface-900">
			<svelte:fragment slot="lead">
				<div class="flex items-center">
					<button class="btn-icon lg:hidden" on:click={() => drawerStore.open(sidebarDrawer)}>
						<Fa icon={faBars} size="lg" />
					</button>
					<a href="/">
						<img src={Logo} alt="The letters DUUI" class="hidden lg:block max-h-8" />
					</a>
				</div>
			</svelte:fragment>
			<svelte:fragment slot="trail">
				<div class="hidden lg:flex items-center gap-4">
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
							<MenuItem href="/pipelines" title="Dashboard" info="View and import Pipelines" />
							<MenuItem
								href="/pipelines/editor"
								title="Editor"
								info="Create Pipelines from sratch or using one of many Templates"
							/>
							<MenuItem
								href="/pipelines/statistics"
								title="Statistics"
								info="View Statistics for your Pipelines and their usage"
							/>
						</svelte:fragment>
					</Menu>
					<Menu label="Documentation" offset={24} placement="bottom-end">
						<svelte:fragment slot="title">
							<span>Documentation</span>
						</svelte:fragment>
						<svelte:fragment slot="content">
							<MenuItem
								href="/documentation#introduction"
								title="Introduction"
								info="Quick overview over the most important features of DUUI"
							/>
							<MenuItem href="/documentation#composer" title="Composer" />
							<MenuItem href="/documentation#driver" title="Driver" />
							<MenuItem href="/documentation#component" title="Component" />
							<MenuItem href="/documentation#document" title="Document" />
							<MenuItem href="/documentation#process" title="Process" />
						</svelte:fragment>
					</Menu>

					<Menu label="API Reference" offset={24} placement="bottom-end">
						<svelte:fragment slot="title">
							<span>API Reference</span>
						</svelte:fragment>
						<svelte:fragment slot="content">
							<MenuItem
								href="/documentation/api#web"
								title="Web"
								info="Help for navigating and using DUUI Web"
							/>
							<MenuItem
								href="/documentation/api#java"
								title="Python"
								info="Tutorials and examples for using DUUI with Python"
							/>
							<MenuItem
								href="/documentation/api#python"
								title="Java"
								info="Tutorials and examples for using DUUI with Java"
							/>
						</svelte:fragment>
					</Menu>
					{#if loggedIn}
						<Menu label="Account" offset={24} placement="bottom-end">
							<svelte:fragment slot="title">
								<span>Account</span>
							</svelte:fragment>
							<svelte:fragment slot="content">
								<MenuItem
									href="/account/user/profile"
									title="Profile"
									info="Manage your Account settings and information"
								/>
								<MenuItem
									href="/account/user/connections"
									title="Connections"
									info="Manage the Connections to different Data Sources as well as your API Key"
								/>
								<MenuItem
									href="/account/user/security"
									title="Security"
									info="Make decisions regarding the security of your Account and Data."
								/>
							</svelte:fragment>
						</Menu>
					{:else}
						<Menu label="Authentication" offset={24} placement="bottom-end">
							<svelte:fragment slot="title">
								<span>Authentication</span>
							</svelte:fragment>
							<svelte:fragment slot="content">
								<MenuItem href="/account/auth/login" title="Login" />
								<MenuItem href="/account/auth/login?register=true" title="Register" />
							</svelte:fragment>
						</Menu>
					{/if}

					<ActionButton
						on:click={logout}
						icon={faArrowRightFromBracket}
						text="Logout"
						variant="hover:text-primary-500 transition-colors flex-row-reverse gap-2 px-0"
					/>
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
