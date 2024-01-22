<script lang="ts">
	import { goto, onNavigate } from '$app/navigation'
	import Link from '$lib/svelte/Link.svelte'
	import { userSession } from '$lib/store'
	import { faArrowRightFromBracket, faBars } from '@fortawesome/free-solid-svg-icons'
	import { Accordion, AccordionItem, LightSwitch, getDrawerStore } from '@skeletonlabs/skeleton'
	import Fa from 'svelte-fa'
	import { sineOut } from 'svelte/easing'

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
</script>

<aside class="space-y-4 z-50">
	<div class="p-4 sticky top-0 dark:bg-surface-900 flex items-center justify-between shadow-lg">
		<button class="btn-icon" on:click={() => drawerStore.close()}>
			<Fa icon={faBars} size="lg" />
		</button>
		{#if $userSession?.role === 'Admin'}
			<span class="badge variant-soft-tertiary font-bold">ADMIN</span>
		{/if}
		<LightSwitch />
	</div>
	<div class="px-4 space-y-4">
		<div class="flex flex-col text-left px-4 space-y-4">
			<Link href="/">Home</Link>
		</div>

		<Accordion spacing="space-y-4" transitionInParams={{ duration: 400, easing: sineOut }}>
			<AccordionItem regionControl="bg-fancy" hover="animate-underline">
				<svelte:fragment slot="summary">Pipelines</svelte:fragment>
				<svelte:fragment slot="content">
					<div class="flex flex-col text-left p-2 space-y-2">
						<Link href="/pipelines">Dashboard</Link>
						<Link href="/pipelines/editor">Editor</Link>
					</div>
				</svelte:fragment>
			</AccordionItem>
			<AccordionItem regionControl="bg-fancy" hover="animate-underline">
				<svelte:fragment slot="summary">Documentation</svelte:fragment>
				<svelte:fragment slot="content">
					<div class="flex flex-col text-left p-2 space-y-2">
						<Link href="/documentation#introduction">Introduction</Link>
						<Link href="/documentation#composer">Composer</Link>
						<Link href="/documentation#driver">Driver</Link>
						<Link href="/documentation#component">Component</Link>
						<Link href="/documentation#io">IO</Link>
					</div>
				</svelte:fragment>
			</AccordionItem>
			<AccordionItem regionControl="bg-fancy" hover="animate-underline">
				<svelte:fragment slot="summary">API Reference</svelte:fragment>
				<svelte:fragment slot="content">
					<div class="flex flex-col text-left p-2 space-y-2">
						<Link href="/documentation/api#rest">REST</Link>
						<Link href="/documentation/api#java">Java</Link>
						<Link href="/documentation/api#python">Python</Link>
					</div>
				</svelte:fragment>
			</AccordionItem>
			<AccordionItem regionControl="bg-fancy" hover="animate-underline">
				<svelte:fragment slot="summary">Account</svelte:fragment>
				<svelte:fragment slot="content">
					<div class="flex flex-col text-left p-2 space-y-2">
						{#if $userSession}
							<Link href="/account#profile">Profile</Link>
							<Link href="/account#authorization">Authorization</Link>
							<button
								class="p-0 btn inline-flex items-center hover:text-primary-500 transition-colors justify-start
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
				</svelte:fragment>
			</AccordionItem>
		</Accordion>
	</div>
</aside>
