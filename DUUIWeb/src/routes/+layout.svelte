<script lang="ts">
	import Logo from '$lib/assets/Logo.svg'
	import '../app.postcss'

	import { faArrowRightFromBracket, faBars } from '@fortawesome/free-solid-svg-icons'
	import {
		AppBar,
		AppShell,
		Drawer,
		LightSwitch,
		Toast,
		getDrawerStore,
		type DrawerSettings,
		type ModalComponent
	} from '@skeletonlabs/skeleton'
	import Fa from 'svelte-fa'

	import { beforeNavigate, goto, onNavigate } from '$app/navigation'
	import { initializeStores, storePopup } from '@skeletonlabs/skeleton'

	import { helpStore, userSession } from '$lib/store'
	import Link from '$lib/svelte/Link.svelte'
	import ConfirmModal from '$lib/svelte/widgets/modal/ConfirmModal.svelte'
	import DeleteModal from '$lib/svelte/widgets/modal/DeleteModal.svelte'
	import DocumentModal from '$lib/svelte/widgets/modal/DocumentModal.svelte'
	import Documentation from '$lib/svelte/widgets/navigation/Documentation.svelte'
	import Sidebar from '$lib/svelte/widgets/navigation/Sidebar.svelte'
	import { arrow, autoUpdate, computePosition, flip, offset, shift } from '@floating-ui/dom'
	import { Modal } from '@skeletonlabs/skeleton'

	import PromptModal from '$lib/svelte/widgets/modal/PromptModal.svelte'
	import Help from '$lib/svelte/widgets/navigation/Help.svelte'
	import HelpToggle from '$lib/svelte/widgets/navigation/HelpToggle.svelte'
	import { storeHighlightJs } from '@skeletonlabs/skeleton'
	import hljs from 'highlight.js/lib/core'
	import java from 'highlight.js/lib/languages/java'
	import xml from 'highlight.js/lib/languages/xml'
	import typescript from 'highlight.js/lib/languages/typescript'

	import 'highlight.js/styles/github-dark.css'
	import ComponentDrawer from '$lib/svelte/widgets/ComponentDrawer.svelte'

	export let data
	let { user } = data
	$userSession = user

	initializeStores()

	const drawerStore = getDrawerStore()
	const sidebarDrawer: DrawerSettings = {
		id: 'sidebar',
		width: 'w-full sm:w-[40%]',
		rounded: 'rounded-none'
	}

	hljs.registerLanguage('java', java)
	hljs.registerLanguage('xml', xml)
	hljs.registerLanguage('ts', typescript)
	storeHighlightJs.set(hljs)

	storePopup.set({ computePosition, autoUpdate, offset, shift, flip, arrow })

	onNavigate(() => {
		drawerStore.close()
	})

	beforeNavigate(() => {
		let expirationDate = $userSession?.expires ? new Date($userSession.expires) : undefined

		if (expirationDate && expirationDate < new Date()) {
			$userSession = null
		}
	})

	const logout = async () => {
		const response = await fetch('/account/logout', { method: 'PUT' })
		if (response.ok) {
			userSession.set(undefined)

			goto('/account/login')
		} else {
			console.error(response.status, response.statusText)
		}
	}

	const modalRegistry: Record<string, ModalComponent> = {
		deleteModal: { ref: DeleteModal },
		documentModal: { ref: DocumentModal },
		promptModal: { ref: PromptModal },
		confirmModal: { ref: ConfirmModal }
	}
</script>

<Modal components={modalRegistry} />
<Toast position="b" />
<Drawer rounded="rounded-md">
	{#if $drawerStore.id === 'sidebar'}
		<Sidebar />
	{:else if $drawerStore.id === 'helpDrawer'}
		<p>
			Lorem, ipsum dolor sit amet consectetur adipisicing elit. Impedit temporibus asperiores aut
			hic, ipsam odio illum voluptate nihil quae laudantium, quibusdam porro, sed aperiam atque
			minus quasi ullam eaque. Soluta?
		</p>
	{/if}
	{#if $drawerStore.id === 'component'}
		<ComponentDrawer />
	{/if}
</Drawer>

<!-- App Shell  -->
<AppShell class="dark:bg-surface-700 ">
	<svelte:fragment slot="header">
		<!-- App Bar -->
		<AppBar shadow="shadow-lg" background="bg-surface-50-900-token z-[100]">
			<svelte:fragment slot="lead">
				<div class="flex items-center gap-4">
					<button class="btn-icon lg:hidden" on:click={() => drawerStore.open(sidebarDrawer)}>
						<Fa icon={faBars} size="lg" />
					</button>
					<a href="/">
						<img src={Logo} alt="The letters DUUI" class="hidden lg:block max-h-8" />
					</a>
					{#if $userSession?.role === 'Admin'}
						<span class="badge variant-soft-tertiary font-bold">ADMIN</span>
					{/if}
					{#if $userSession?.role === 'Trial'}
						<span class="badge variant-soft-error font-bold">TRIAL</span>
					{/if}
				</div>
			</svelte:fragment>
			<svelte:fragment slot="trail">
				<div class="hidden lg:flex items-center gap-8 font-heading-token">
					{#if $userSession?.role === 'Admin' || $userSession?.role === 'Trial'}
						<Link href="/feedback">Feedback</Link>
					{/if}
					{#if $userSession}
						<Link href="/pipelines">Pipelines</Link>
						<Link href="/pipelines/editor">Editor</Link>
					{/if}
					<Link href="/documentation">Documentation</Link>
					<Link href="/documentation/api">API Reference</Link>

					{#if $userSession}
						<Link href="/account">Account</Link>
						<button
							class="p-0 btn inline-flex items-center hover:text-primary-500 transition-colors
							animate-underline"
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

	<svelte:fragment slot="sidebarLeft">
		<Documentation />
	</svelte:fragment>

	<!-- Sidebar on the right -->
	<HelpToggle />
	<svelte:fragment slot="sidebarRight">
		{#if $helpStore}
			<Help />
		{/if}
	</svelte:fragment>

	<!-- Page Route Content -->
	<slot />
</AppShell>
