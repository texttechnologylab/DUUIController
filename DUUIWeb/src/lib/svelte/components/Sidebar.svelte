<script lang="ts">
	import { goto, onNavigate } from '$app/navigation'
	import { isDarkModeStore, userSession } from '$lib/store'
	import { faGithub, faReadme, faXTwitter } from '@fortawesome/free-brands-svg-icons'
	import {
		faBars,
		faBook,
		faGlobe,
		faHome,
		faLayerGroup,
		faMapSigns,
		faQuestion,
		faTools,
		faUser
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

<aside class="space-y-4 z-50 bg-surface-50-900-token h-full flex flex-col">
	<div class="p-4 sticky top-0 bg-surface-50-900-token flex items-center justify-between shadow-md">
		<button class="btn-icon" on:click={() => drawerStore.close()}>
			<Fa icon={faBars} size="lg" />
		</button>
		{#if $userSession?.role === 'Admin'}
			<span class="badge variant-soft-tertiary font-bold">ADMIN</span>
		{/if}
		<LightSwitch rounded="rounded-full" on:click={() => ($isDarkModeStore = !$isDarkModeStore)} />
	</div>
	<div class="p-8 space-y-8 text-xl h-full">
		<a href="/" class="flex items-center gap-4 animate-text">
			<Fa icon={faHome} />
			Home
		</a>
		<a href="/pipelines" class="flex items-center gap-4 animate-text">
			<Fa icon={faLayerGroup} />
			Pipelines
		</a>
		<a href="/pipelines/build" class="flex items-center gap-4 animate-text">
			<Fa icon={faTools} />
			Builder
		</a>
		<a href="/documentation" class="flex items-center gap-4 animate-text">
			<Fa icon={faBook} />
			Documentation
		</a>
		<a href="/documentation/api" class="flex items-center gap-4 animate-text">
			<Fa icon={faReadme} />
			API Reference
		</a>
		<a href="/account" class="flex items-center gap-4 animate-text">
			<Fa icon={faUser} />
			Account
		</a>
		<button
			class="flex items-center gap-4 animate-text"
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
	<div class="flex flex-col md:flex-row justify-center items-center md:items-start gap-4">
		<div class="flex items-center gap-8 p-4 variant-filled-primary w-full justify-center">
			<a
				target="_blank"
				href="https://github.com/texttechnologylab"
				class="transition-opacity opacity-70 hover:opacity-100"
			>
				<Fa icon={faGithub} size="2x" />
			</a>
			<a
				target="_blank"
				href="https://twitter.com/ttlab_ffm"
				class="transition-opacity opacity-70 hover:opacity-100"
			>
				<Fa icon={faXTwitter} size="2x" />
			</a>
			<a
				target="_blank"
				href="https://www.texttechnologylab.org/"
				class="transition-opacity opacity-70 hover:opacity-100"
			>
				<Fa icon={faGlobe} size="2x" />
			</a>
		</div>
	</div>
</aside>
