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
		faUserPlus,
		faLock,
		faLink,
		faHammer,

		faCloud,

		faGear


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
			goto('/account/auth/login', { invalidateAll: true })
		}
	}
</script>

<aside class="space-y-4 z-50">
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
							href="/pipelines/editor"
							icon={faHammer}
							size="lg"
							text="Editor"
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
							href="/documentation"
							icon={faGears}
							text="Docker Unified UIMA Interface"
							_class="justify-start gap-8 bg-primary-hover-token"
							variant=""
						/>
						<Anchor
							href="/documentation/api"
							icon={faCloud}
							text="API Reference"
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
						{#if !loggedIn}
							<Anchor
								href="/account/auth/login"
								icon={faArrowRightToBracket}
								text="Login"
								_class="justify-start gap-8 bg-primary-hover-token"
								variant=""
							/>
							<Anchor
								href="/account/auth/login?register=true"
								icon={faUserPlus}
								text="Register"
								_class="justify-start gap-6 bg-primary-hover-token"
								variant=""
							/>
						{:else}
							<Anchor
								href="/account/user/profile"
								icon={faUser}
								text="Profile"
								_class="justify-start gap-8 bg-primary-hover-token"
								variant=""
							/>
							<Anchor
								href="/account/user/connections"
								icon={faLink}
								text="Connections"
								_class="justify-start gap-8 bg-primary-hover-token"
								variant=""
							/>
							<Anchor
								href="/account/user/security"
								icon={faLock}
								text="Security"
								_class="justify-start gap-8 bg-primary-hover-token"
								variant=""
							/>
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
