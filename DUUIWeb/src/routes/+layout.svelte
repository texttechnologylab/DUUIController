<script lang="ts">
	import Logo from '$lib/assets/Logo.svg'
	import '../app.postcss'

	import {
		faArrowRightFromBracket,
		faArrowRightToBracket,
		faBars,
		faBook,
		faChevronDown,
		faLayerGroup,
		faMapSigns,
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
		type DrawerSettings,
		type ModalComponent
	} from '@skeletonlabs/skeleton'
	import Fa from 'svelte-fa'

	import { afterNavigate, beforeNavigate, goto, onNavigate } from '$app/navigation'
	import { initializeStores, storePopup } from '@skeletonlabs/skeleton'

	import { isDarkModeStore, userSession } from '$lib/store'
	import Documentation from '$lib/svelte/components/Documentation.svelte'
	import DocumentModal from '$lib/svelte/components/Drawer/DocumentDrawer.svelte'
	import Sidebar from '$lib/svelte/components/Drawer/Sidebar.svelte'
	import Link from '$lib/svelte/components/Link.svelte'
	import ConfirmModal from '$lib/svelte/components/Modal/ConfirmModal.svelte'
	import HelpModal from '$lib/svelte/components/Modal/HelpModal.svelte'
	import PromptModal from '$lib/svelte/components/Modal/PromptModal.svelte'
	import { arrow, autoUpdate, computePosition, flip, offset, shift } from '@floating-ui/dom'
	import { Modal } from '@skeletonlabs/skeleton'

	import { storeHighlightJs } from '@skeletonlabs/skeleton'
	import hljs from 'highlight.js/lib/core'
	import java from 'highlight.js/lib/languages/java'
	import python from 'highlight.js/lib/languages/python'
	import typescript from 'highlight.js/lib/languages/typescript'
	import xml from 'highlight.js/lib/languages/xml'

	import { COLORS } from '$lib/config'
	import ComponentDrawer from '$lib/svelte/components/Drawer/ComponentDrawer.svelte'
	import DocumentDrawer from '$lib/svelte/components/Drawer/DocumentDrawer.svelte'
	import TemplateModal from '$lib/svelte/components/Modal/TemplateModal.svelte'
	import Popup from '$lib/svelte/components/Popup.svelte'
	import { faReadme } from '@fortawesome/free-brands-svg-icons'
	import type { AfterNavigate } from '@sveltejs/kit'
	import 'highlight.js/styles/github-dark.css'
	import { onMount } from 'svelte'
	import ProcessDrawer from './processes/[oid]/ProcessDrawer.svelte'

	export let data
	let { user, theme } = data
	$userSession = user

	const themes = Object.keys(COLORS)
	$: {
		try {
			const body = document.body
			body.dataset.theme = 'theme-' + themes[theme]
		} catch (err) {}
	}

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

	afterNavigate((params: AfterNavigate) => {
		const isNewPage = params.from?.url.pathname !== params.to?.url.pathname
		const elemPage = document.querySelector('#page')
		if (isNewPage && elemPage !== null) {
			elemPage.scrollTop = 0
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
		helpModal: { ref: HelpModal },
		templateModal: { ref: TemplateModal }
	}
	const modalStore = getModalStore()
</script>

<Modal components={modalRegistry} />
<Toast position="b" />
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

<!-- App Shell  -->
<AppShell>
	<svelte:fragment slot="pageHeader">
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
					{#if $userSession}
						<Link href="/feedback">Feedback</Link>
						<Popup>
							<svelte:fragment slot="trigger">
								<button
									class="p-0 btn inline-flex items-center animate-underline transition-300 group-hover:text-primary-500"
								>
									<span>Pipelines</span>
									<Fa icon={faChevronDown} />
								</button>
							</svelte:fragment>
							<svelte:fragment slot="popup">
								<div class="popup-solid p-4 flex flex-col gap-4">
									<a href="/pipelines" class="anchor-neutral border-none !gap-8"
										><Fa icon={faLayerGroup} /><span>Dashboard</span></a
									>
									<a href="/pipelines/build" class="anchor-neutral border-none !gap-8"
										><Fa icon={faTools} /><span>Builder</span></a
									>
								</div>
							</svelte:fragment>
						</Popup>
					{/if}
					<Popup>
						<svelte:fragment slot="trigger">
							<button
								class="p-0 btn inline-flex items-center animate-underline transition-300 hover:text-primary-500"
							>
								<span>Documentation</span>
								<Fa icon={faChevronDown} />
							</button>
						</svelte:fragment>
						<svelte:fragment slot="popup">
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
						</svelte:fragment>
					</Popup>

					<Popup>
						<svelte:fragment slot="trigger">
							<button
								class="p-0 btn inline-flex items-center animate-underline transition-300 hover:text-primary-500"
							>
								<span>Account</span>
								<Fa icon={faChevronDown} />
							</button>
						</svelte:fragment>
						<svelte:fragment slot="popup">
							<div class="popup-solid p-4 flex flex-col gap-4 -translate-x-1/4">
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
						</svelte:fragment>
					</Popup>
					<LightSwitch
						class="md:block hidden mx-auto border bordered-soft"
						rounded="rounded-full"
						on:click={() => ($isDarkModeStore = !$isDarkModeStore)}
					/>
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
