<script lang="ts">
	import Logo from '$lib/assets/Logo.svg'
	import '../app.postcss'

	import {
		faArrowRightFromBracket,
		faArrowRightToBracket,
		faBars,
		faBook,
		faCheck,
		faChevronDown,
		faLayerGroup,
		faMapSigns,
		faPaintBrush,
		faTools,
		faUser,
		faUserPlus
	} from '@fortawesome/free-solid-svg-icons'
	import {
		AppBar,
		AppShell,
		Drawer,
		LightSwitch,
		Toast,
		getDrawerStore,
		getModalStore,
		popup,
		type DrawerSettings,
		type ModalComponent,
		type PopupSettings
	} from '@skeletonlabs/skeleton'
	import Fa from 'svelte-fa'

	import { beforeNavigate, goto, onNavigate } from '$app/navigation'
	import { initializeStores, storePopup } from '@skeletonlabs/skeleton'

	import { isDarkModeStore, userSession } from '$lib/store'
	import ConfirmModal from '$lib/svelte/components/ConfirmModal.svelte'
	import Documentation from '$lib/svelte/components/Documentation.svelte'
	import DocumentModal from '$lib/svelte/components/Drawer/DocumentDrawer.svelte'
	import Link from '$lib/svelte/components/Link.svelte'
	import HelpModal from '$lib/svelte/components/Modal/HelpModal.svelte'
	import PromptModal from '$lib/svelte/components/Modal/PromptModal.svelte'
	import WelcomeModal from '$lib/svelte/components/Modal/WelcomeModal.svelte'
	import Sidebar from '$lib/svelte/components/Sidebar.svelte'
	import { arrow, autoUpdate, computePosition, flip, offset, shift } from '@floating-ui/dom'
	import { Modal } from '@skeletonlabs/skeleton'

	import { storeHighlightJs } from '@skeletonlabs/skeleton'
	import hljs from 'highlight.js/lib/core'
	import java from 'highlight.js/lib/languages/java'
	import python from 'highlight.js/lib/languages/python'
	import typescript from 'highlight.js/lib/languages/typescript'
	import xml from 'highlight.js/lib/languages/xml'

	import ComponentDrawer from '$lib/svelte/components/Drawer/ComponentDrawer.svelte'
	import DocumentDrawer from '$lib/svelte/components/Drawer/DocumentDrawer.svelte'
	import TemplateModal from '$lib/svelte/components/Modal/TemplateModal.svelte'
	import 'highlight.js/styles/github-dark.css'
	import { onMount } from 'svelte'
	import ProcessDrawer from './processes/[oid]/ProcessDrawer.svelte'
	import { toTitleCase } from '$lib/duui/utils/text'
	import { COLORS } from '$lib/config'
	import { faReadme } from '@fortawesome/free-brands-svg-icons'

	export let data
	let { user, theme } = data
	$userSession = user

	initializeStores()

	const drawerStore = getDrawerStore()
	const sidebarDrawer: DrawerSettings = {
		id: 'sidebar',
		width: 'w-full sm:w-1/2',
		rounded: 'rounded-none'
	}

	hljs.registerLanguage('java', java)
	hljs.registerLanguage('xml', xml)
	hljs.registerLanguage('ts', typescript)
	hljs.registerLanguage('py', python)
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

	onMount(() => {
		const html = document.getElementsByTagName('html').item(0)
		if (html) {
			$isDarkModeStore = html.classList.contains('dark')
		}
	})

	const modalRegistry: Record<string, ModalComponent> = {
		documentModal: { ref: DocumentModal },
		promptModal: { ref: PromptModal },
		confirmModal: { ref: ConfirmModal },
		welcomeModal: { ref: WelcomeModal },
		helpModal: { ref: HelpModal },
		templateModal: { ref: TemplateModal }
	}
	const modalStore = getModalStore()

	const themePopup: PopupSettings = {
		event: 'click',
		target: 'theme-popup',
		middleware: {
			offset: 24
		},
		placement: 'bottom'
	}

	const pipelinesPopup: PopupSettings = {
		event: 'click',
		target: 'pipelines-popup',
		middleware: {
			offset: 24
		},
		placement: 'bottom'
	}

	const documentationPopup: PopupSettings = {
		event: 'click',
		target: 'documentation-popup',
		middleware: {
			offset: 24
		},
		placement: 'bottom'
	}

	const accountPopup: PopupSettings = {
		event: 'click',
		target: 'account-popup',
		middleware: {
			offset: 24
		},
		placement: 'bottom'
	}

	const updateTheme = async (color: string) => {
		const response = await fetch(`/api/theme?color=${color}`, {
			method: 'PUT'
		})

		if (response.ok) {
			const result = await response.json()
			theme = +result.theme
		}
	}

	const themes = ['blue', 'red', 'purple']
	$: {
		try {
			const body = document.body
			body.dataset.theme = 'theme-' + themes[theme]
		} catch (err) {}
	}
</script>

<Modal components={modalRegistry} />
<Toast position="br" />
<Drawer rounded="rounded-md">
	{#if $drawerStore.id === 'sidebar'}
		<Sidebar />
	{:else if $drawerStore.id === 'document'}
		<DocumentDrawer />
	{:else if $drawerStore.id === 'component'}
		<ComponentDrawer />
	{:else if $drawerStore.id === 'process'}
		<ProcessDrawer />
	{/if}
</Drawer>

<div data-popup="theme-popup" class="z-[100]">
	<div class="popup-solid p-4 flex flex-col gap-4">
		{#each themes as color, index}
			<button class="button-neutral border-none !gap-8" on:click={() => updateTheme(color)}>
				<Fa icon={faCheck} class={index === +theme ? '' : 'invisible'} />
				{toTitleCase(color)}
			</button>
		{/each}

		<LightSwitch
			class="md:block hidden mx-auto border bordered-soft"
			rounded="rounded-full"
			on:click={() => ($isDarkModeStore = !$isDarkModeStore)}
		/>
	</div>
	<div
		class="arrow !left-1/2 !w-4 !h-4 !-translate-x-1/2 bg-surface-50-900-token border-l border-t border-color"
	/>
</div>

<div data-popup="pipelines-popup" class="z-[100]">
	<div class="popup-solid p-4 flex flex-col gap-4">
		<a href="/pipelines" class="anchor-neutral border-none !gap-8"
			><Fa icon={faLayerGroup} /><span>Dashboard</span></a
		>
		<a href="/pipelines/build" class="anchor-neutral border-none !gap-8"
			><Fa icon={faTools} /><span>Builder</span></a
		>
	</div>
	<div
		class="arrow !left-1/2 !w-4 !h-4 !-translate-x-1/2 bg-surface-50-900-token border-l border-t border-color"
	/>
</div>

<div data-popup="documentation-popup" class="z-[100]">
	<div class="popup-solid p-4 flex flex-col gap-4">
		<a href="/documentation" class="anchor-neutral border-none !gap-8"
			><Fa icon={faBook} /><span>Documentation</span></a
		>
		<a href="/documentation/api" class="anchor-neutral border-none !gap-8"
			><Fa icon={faReadme} /><span>API Reference</span></a
		>
		<button
			class="button-neutral border-none !gap-8"
			on:click={() => {
				modalStore.trigger({
					type: 'component',
					component: 'helpModal'
				})
			}}
		>
			<Fa icon={faMapSigns} />
			<span>Help</span>
		</button>
	</div>
	<div
		class="arrow !left-1/2 !w-4 !h-4 !-translate-x-1/2 bg-surface-50-900-token border-l border-t border-color"
	/>
</div>

<div data-popup="account-popup" class="z-[100]">
	<div class="popup-solid p-4 flex flex-col gap-4">
		{#if $userSession}
			<a href="/account" class="anchor-neutral border-none !gap-8"
				><Fa icon={faUser} /><span>Profile</span></a
			>
			<button class="button-neutral border-none !gap-8" on:click={logout}>
				<Fa icon={faArrowRightFromBracket} />
				<span>Logout</span>
			</button>
		{:else}
			<a href="/account/login" class="anchor-neutral border-none !gap-8"
				><Fa icon={faArrowRightToBracket} /><span>Login</span></a
			>
			<a href="/account/register" class="anchor-neutral border-none !gap-8"
				><Fa icon={faUserPlus} /><span>Register</span></a
			>
		{/if}
	</div>
	<div
		class="arrow !left-1/2 !w-4 !h-4 !-translate-x-1/2 bg-surface-50-900-token border-l border-t border-color"
	/>
</div>

<!-- App Shell  -->
<AppShell>
	<svelte:fragment slot="header">
		<!-- App Bar -->
		<AppBar class="border-b border-color z-[100]" background="bg-surface-50-900-token">
			<svelte:fragment slot="lead">
				<div class="flex-center-4">
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
						<button
							class="p-0 btn inline-flex items-center animate-underline transition-300 hover:text-primary-500"
							use:popup={pipelinesPopup}
						>
							<span>Pipelines</span>
							<Fa icon={faChevronDown} />
						</button>
					{/if}
					<button
						class="p-0 btn inline-flex items-center animate-underline transition-300 hover:text-primary-500"
						use:popup={documentationPopup}
					>
						<span>Documentation</span>
						<Fa icon={faChevronDown} />
					</button>
					<button
						class="p-0 btn inline-flex items-center animate-underline transition-300 hover:text-primary-500"
						use:popup={themePopup}
					>
						<span>Theme</span>
						<Fa icon={faChevronDown} />
					</button>
					<button
						class="p-0 btn inline-flex items-center animate-underline transition-300 hover:text-primary-500"
						use:popup={accountPopup}
					>
						<span>Account</span>
						<Fa icon={faChevronDown} />
					</button>
				</div>

				<a href="/">
					<img src={Logo} alt="The letters DUUI" class="md:hidden block max-h-8 pr-4" />
				</a>
			</svelte:fragment>
		</AppBar>
	</svelte:fragment>

	<svelte:fragment slot="sidebarLeft">
		<Documentation />
	</svelte:fragment>

	<!-- Page Route Content -->
	<slot />
</AppShell>
