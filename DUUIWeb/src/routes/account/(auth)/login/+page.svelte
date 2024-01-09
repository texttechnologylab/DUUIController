<script lang="ts">
	import { goto } from '$app/navigation'
	import { page } from '$app/stores'
	import { userSession } from '$lib/store'
	import ActionButton from '$lib/svelte/widgets/action/ActionButton.svelte'
	import Password from '$lib/svelte/widgets/input/Password.svelte'
	import HLine from '$lib/svelte/widgets/navigation/HLine.svelte'
	import { faExclamationTriangle, faArrowRightToBracket } from '@fortawesome/free-solid-svg-icons'
	import Fa from 'svelte-fa'
	import { fly } from 'svelte/transition'
	import Text from '$lib/svelte/widgets/input/TextInput.svelte'
	import Link from '$lib/svelte/components/Link.svelte'
	import type { ActionData } from '../../../pipelines/[oid]/$types'

	let email: string = $page.url.searchParams.get('email') || ''
	let password: string

	export let form: ActionData

	let redirectTo: string = $page.url.searchParams.get('redirectTo') || '/account/user/profile'

	let message: string
	$: message = $page.url.searchParams.get('message') ?? ''

	const loginUser = async () => {
		const response = await fetch('/auth/login', {
			method: 'POST',
			body: JSON.stringify({
				email: email,
				password: password
			}),
			headers: {
				'Content-Type': 'application/json'
			}
		})

		const result = await response.json()

		if (response.ok) {
			userSession.set(result.user)
			goto('/account')
		}
	}
</script>

<svelte:head>
	<title>Login</title>
</svelte:head>

<div class="space-y-4 w-modal md:m-16">
	{#if message}
		<div
			class="flex items-start justify-between variant-filled-warning
            p-4 gap-4 shadow-lg rounded-md"
		>
			<p>{message}</p>
			<Fa icon={faExclamationTriangle} size="lg" />
		</div>
	{/if}

	{#if form?.error}
		<div class="flex items-start justify-between variant-filled-error p-4">
			<p>{form.error}</p>
			<Fa icon={faExclamationTriangle} size="lg" />
		</div>
	{/if}

	<div
		class="rounded-md border bg-surface-100 dark:variant-soft-surface shadow-lg border-surface-400/20"
		in:fly={{ x: -300 }}
	>
		<div class="p-4 sm:p-8 space-y-4 transition-transform duration-300">
			<h2 class="h1 font-bold sm:h2">Login</h2>
			<div class="space-y-4 py-4">
				<Text bind:value={email} name="email" label="Email" />
				<Text name="redirect" label="redirect" hidden={true} bind:value={redirectTo} />
				<Password name="password" label="Password" bind:value={password} />
				<ActionButton
					on:click={loginUser}
					icon={faArrowRightToBracket}
					text="Login"
					variant="variant-filled-primary dark:variant-soft-primary"
				/>
			</div>

			<HLine width="w-full rounded-full mx-auto" thickness={2} />
			<div class="flex items-center justify-between">
				<p>
					<span>Don't have an Account?</span>
					<Link href="/account/register" underline={true}>Register</Link>
				</p>
				<Link href="/account/recover" underline={true}>Forgot password?</Link>
			</div>
		</div>
	</div>
</div>
