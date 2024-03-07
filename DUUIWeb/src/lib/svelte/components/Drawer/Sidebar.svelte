<!--	
	@component
	The mobile navigation menu.
-->
<script lang="ts">
	import { goto, onNavigate } from '$app/navigation'
	import { isDarkModeStore, userSession } from '$lib/store'
	import { faGithub, faReadme, faXTwitter } from '@fortawesome/free-brands-svg-icons'
	import {
		faArrowRightFromBracket,
		faArrowRightToBracket,
		faBars,
		faBook,
		faClipboardQuestion,
		faGlobe,
		faHome,
		faLayerGroup,
		faMapSigns,
		faTools,
		faUser,
		faUserPlus
	} from '@fortawesome/free-solid-svg-icons'
	import { LightSwitch, getDrawerStore, getModalStore } from '@skeletonlabs/skeleton'
	import Fa from 'svelte-fa'

	const drawerStore = getDrawerStore()

	const logout = async () => {
		const response = await fetch('/account/logout', { method: 'PUT' })
		if (response.ok) {
			userSession.set(undefined)

			goto('/account/login')
		} else {
			console.error(response.status, response.statusText)
		}
	}

	onNavigate(() => {
		drawerStore.close()
	})

	const modalStore = getModalStore()
</script>

<aside class=" z-50 bg-surface-100-800-token h-full flex flex-col md:border-r border-color">
	<div
		class="p-4 sticky top-0 bg-surface-50-900-token border-color border-b flex items-center justify-between"
	>
		<button class="btn-icon" on:click={() => drawerStore.close()}>
			<Fa icon={faBars} size="lg" />
		</button>
		{#if $userSession?.role === 'Admin'}
			<span class="badge variant-soft-tertiary font-bold">ADMIN</span>
		{/if}
		<LightSwitch rounded="rounded-full" on:click={() => ($isDarkModeStore = !$isDarkModeStore)} />
	</div>
	<div class="p-8 space-y-8 text-xl h-full">
		<a href="/" class="flex-center-4 transition-300 hover:text-primary-500">
			<Fa icon={faHome} />
			Home
		</a>
		{#if $userSession}
			<a href="/pipelines" class="flex-center-4 transition-300 hover:text-primary-500">
				<Fa icon={faLayerGroup} />
				Pipelines
			</a>
			<a href="/pipelines/build" class="flex-center-4 transition-300 hover:text-primary-500">
				<Fa icon={faTools} />
				Builder
			</a>
		{/if}
		<a href="/documentation" class="flex-center-4 transition-300 hover:text-primary-500">
			<Fa icon={faBook} />
			Documentation
		</a>
		<a href="/documentation/api" class="flex-center-4 transition-300 hover:text-primary-500">
			<Fa icon={faReadme} />
			API Reference
		</a>

		<button
			class="flex-center-4 transition-300 hover:text-primary-500"
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

		{#if $userSession}
			<a href="/feedback" class="flex-center-4 transition-300 hover:text-primary-500">
				<Fa icon={faClipboardQuestion} />
				Feedback
			</a>
			<a href="/account" class="flex-center-4 transition-300 hover:text-primary-500">
				<Fa icon={faUser} />
				Account
			</a>
			<button class="flex-center-4 transition-300 hover:text-primary-500" on:click={logout}>
				<Fa icon={faArrowRightFromBracket} />
				<span>Logout</span>
			</button>
		{:else}
			<a href="/account/login" class="flex-center-4 transition-300 hover:text-primary-500"
				><Fa icon={faArrowRightToBracket} /><span>Login</span></a
			>
			<a href="/account/register" class="flex-center-4 transition-300 hover:text-primary-500"
				><Fa icon={faUserPlus} /><span>Register</span></a
			>
		{/if}
	</div>
	<div
		class="flex flex-col md:flex-row justify-center items-center md:items-start gap-4 border-t border-color bg-surface-50-900-token"
	>
		<div class="flex items-center gap-8 p-4 w-full justify-center">
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
</aside>
