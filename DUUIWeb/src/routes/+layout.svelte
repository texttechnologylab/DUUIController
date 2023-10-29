<script lang="ts">
	import '../app.postcss'
	import Logo from '$lib/assets/Logo.png'
	import { AppShell, AppBar, LightSwitch } from '@skeletonlabs/skeleton'
	import { Drawer, getDrawerStore } from '@skeletonlabs/skeleton'
	import { Toast, type DrawerSettings } from '@skeletonlabs/skeleton'
	import Fa from 'svelte-fa'
	import { faGlobe, faBars } from '@fortawesome/free-solid-svg-icons'
	import { faGithub } from '@fortawesome/free-brands-svg-icons'

	import { initializeStores } from '@skeletonlabs/skeleton'
	import { onNavigate } from '$app/navigation'
	import SidebarNav from '$lib/components/SidebarNav.svelte'
	import { storePopup } from '@skeletonlabs/skeleton'

	import { computePosition, autoUpdate, offset, shift, flip, arrow } from '@floating-ui/dom'
	import ComponentEditor from '$lib/components/ComponentEditor.svelte'
	import { Modal } from '@skeletonlabs/skeleton'

	initializeStores()

	const drawerStore = getDrawerStore()

	storePopup.set({ computePosition, autoUpdate, offset, shift, flip, arrow })

	const openDocsMenu = () => {
		const settings: DrawerSettings = {
			id: 'docs',
			width: 'max-w-fit',
			border: 'border-r-2 border-surface-400',
			bgBackdrop: 'variant-glass-surface'
		}
		drawerStore.open(settings)
	}

	onNavigate(() => {
		drawerStore.close()
	})

	export let data
</script>

<Modal />
<Toast />
<Drawer>
	{#if $drawerStore.id === 'docs'}
		<SidebarNav hidden={false} withNavigation={true} />
	{:else if $drawerStore.id === 'component'}
		<ComponentEditor component={$drawerStore.meta.component} />
	{/if}
</Drawer>
<!-- App Shell -->
<AppShell>
	<svelte:fragment slot="header">
		<!-- App Bar -->
		<AppBar gridColumns="grid-cols-3" slotDefault="place-self-center" slotTrail="place-content-end">
			<svelte:fragment slot="lead">
				<div class="grid grid-cols-2 items-center">
					<button class="ml-auto lg:mr-auto btn-icon lg:hidden" on:click={() => openDocsMenu()}
						><Fa size="lg" icon={faBars} /></button
					>
					<a href="/" class="hidden md:block text-xl font-mono font-bold uppercase">DUUI</a>
				</div>
			</svelte:fragment>
			<svelte:fragment slot="trail">
				<div class="hidden lg:flex items-center gap-4">
					<a
						class="btn btn-sm hover:variant-ghost-primary rounded-sm"
						href="/pipelines"
						rel="noreferrer"
					>
						Pipelines
					</a>
					<a
						class="btn btn-sm hover:variant-ghost-primary rounded-sm"
						href="/docs"
						rel="noreferrer"
					>
						Documentation
					</a>
					<a
						class="btn btn-sm hover:variant-ghost-primary rounded-sm"
						href="/user/login"
						rel="noreferrer">{data.user ? 'Logout' : 'Login'}</a
					>
				</div>
				<LightSwitch rounded="rounded-full" />
			</svelte:fragment>
		</AppBar>
	</svelte:fragment>

	<!-- Page Route Content -->

	<slot />

	<svelte:fragment slot="pageFooter">
		<footer id="page-footer" class="flex-none">
			<div
				class="page-footer bg-surface-50 dark:bg-surface-900 border-t border-surface-500/10 text-xs md:text-base"
			>
				<div class="w-full max-w-7xl mx-auto space-y-4 py-8">
					<div class="flex flex-col md:flex-row justify-center space-y-8">
						<div class="grid grid-cols-1 gap-4 place-items-center">
							<img src={Logo} class="max-h-14" alt="" />
							<p class="!text-sm opacity-80">
								<span class="text-primary-400 font-bold">DUUI</span> - Lightweight NLP Framework
							</p>
						</div>
					</div>
					<div
						class="flex flex-col md:flex-row justify-center items-center md:items-start space-y-4 md:space-y-0"
					>
						<div class="flex space-x-4">
							<a
								class="btn variant-soft"
								href="https://github.com/texttechnologylab/DUUIController"
								target="_blank"
								rel="noreferrer"
							>
								<Fa icon={faGithub} />
								<span class="hidden md:inline-block ml-2">Github</span></a
							>
							<a
								class="btn variant-soft"
								href="https://www.texttechnologylab.org/"
								target="_blank"
								rel="noreferrer"
							>
								<Fa icon={faGlobe} />
								<span class="hidden md:inline-block ml-2">TTLab</span></a
							>
						</div>
					</div>
				</div>
			</div>
		</footer>
	</svelte:fragment>
</AppShell>


