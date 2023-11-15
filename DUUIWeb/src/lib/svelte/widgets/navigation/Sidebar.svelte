<script lang="ts">
	import Logo from '$lib/assets/Logo.svg'
	import Icon from '$lib/assets/favicon.svg'

	import {
		faArrowRightFromBracket,
		faArrowRightToBracket,
		faBars,
		faBookOpen,
		faGears,
		faHome,
		faLayerGroup,
		faNetworkWired,
		faPlus,
		faMap,
		faUser,
		faUserPlus
	} from '@fortawesome/free-solid-svg-icons'
	import { Accordion, AccordionItem, LightSwitch, getDrawerStore } from '@skeletonlabs/skeleton'
	import Fa from 'svelte-fa'
	import Anchor from '../action/Anchor.svelte'
	import ActionButton from '../action/ActionButton.svelte'
	import { goto } from '$app/navigation'
	import { makeApiCall, Api } from '$lib/utils/api'

	const drawerStore = getDrawerStore()
	export let loggedIn: boolean = false

	const logout = async () => {
		const response = await makeApiCall(Api.Logout, 'PUT', {})
		if (response.ok) {
			loggedIn = false
			goto('/account/login', { invalidateAll: true })
		}
	}
</script>

<aside class="space-y-4">
	<div class="p-4 sticky top-0 dark:bg-surface-900 flex items-center justify-between shadow-lg">
		<button class="btn-icon" on:click={() => drawerStore.close()}>
			<Fa icon={faBars} size="lg" />
		</button>
		<LightSwitch />
	</div>
	<div class="px-4 space-y-4">
		<Anchor
			href="/"
			icon={faHome}
			text="Home"
			_class="items-center flex justify-start block gap-8 bg-primary-hover-token"
			variant=""
		/>
		<Accordion spacing="space-y-4">
			<AccordionItem rounded="rounded-none">
				<svelte:fragment slot="summary">Pipelines</svelte:fragment>
				<svelte:fragment slot="content">
					<div class="flex flex-col text-left">
						<Anchor
							href="/pipelines"
							icon={faLayerGroup}
							text="Dashboard"
							_class="justify-start gap-8 bg-primary-hover-token"
							variant=""
						/>
						<Anchor
							href="/pipelines/new"
							icon={faPlus}
							size="lg"
							text="Builder"
							_class="justify-start gap-8 bg-primary-hover-token"
							variant=""
						/>
					</div>
				</svelte:fragment>
			</AccordionItem>
			<AccordionItem rounded="rounded-none">
				<svelte:fragment slot="summary">Documentation</svelte:fragment>
				<svelte:fragment slot="content">
					<div class="flex flex-col text-left">
						<Anchor
							href="/documentation#quick-start"
							icon={faBookOpen}
							text="Quick Start"
							_class="justify-start gap-8 bg-primary-hover-token"
							variant=""
						/>
						<Anchor
							href="/documentation#composer"
							icon={faGears}
							text="Composer"
							_class="justify-start gap-8 bg-primary-hover-token"
							variant=""
						/>
						<Anchor
							href="/documentation#driver"
							icon={faNetworkWired}
							text="Driver"
							_class="justify-start gap-8 bg-primary-hover-token"
							variant=""
						/>
						<Anchor
							href="/documentation#component"
							icon={faMap}
							text="Component"
							_class="justify-start gap-8 bg-primary-hover-token"
							variant=""
						/>
					</div>
				</svelte:fragment>
			</AccordionItem>
			<AccordionItem rounded="rounded-none">
				<svelte:fragment slot="summary">Account</svelte:fragment>
				<svelte:fragment slot="content">
					<div class="flex flex-col text-left">
						<Anchor
							href="/account"
							icon={faUser}
							text="Profile"
							_class="justify-start gap-8 bg-primary-hover-token"
							variant=""
						/>
						{#if !loggedIn}
							<Anchor
								href="/account/login"
								icon={faArrowRightToBracket}
								text="Login"
								_class="justify-start gap-8 bg-primary-hover-token"
								variant=""
							/>
							<Anchor
								href="/account/login?register=true"
								icon={faUserPlus}
								text="Register"
								_class="justify-start gap-6 bg-primary-hover-token"
								variant=""
							/>
						{:else}
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
			</AccordionItem>
		</Accordion>
	</div>
</aside>
