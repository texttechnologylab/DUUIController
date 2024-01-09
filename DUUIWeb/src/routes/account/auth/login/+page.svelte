<script lang="ts">
	import { page } from '$app/stores'
	import {
		faArrowRightToBracket,
		faExclamationTriangle,
		faUserCheck
	} from '@fortawesome/free-solid-svg-icons'
	import type { ActionData } from './$types'
	import ActionButton from '$lib/svelte/widgets/action/ActionButton.svelte'
	import Fa from 'svelte-fa'
	import Text from '$lib/svelte/widgets/input/TextInput.svelte'
	import Password from '$lib/svelte/widgets/input/Password.svelte'
	import { fly } from 'svelte/transition'
	import { userSession } from '$lib/store'
	import { goto } from '$app/navigation'
	import HLine from '$lib/svelte/widgets/navigation/HLine.svelte'

	let email: string = $page.url.searchParams.get('email') || ''
	let password: string

	export let form: ActionData

	let register: boolean = $page.url.searchParams.get('register') === 'true' || false
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
			goto('/pipelines')
		}
	}
</script>

<svelte:head>
	<title>Login</title>
</svelte:head>

<div class="space-y-4 py-4 w-modal">
	{#if message}
		<div class="flex items-start justify-between variant-filled-warning p-4 gap-4 shadow-lg">
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

	{#if !register}
		<div
			class="rounded-md border-[1px] bg-surface-100 dark:variant-soft-surface shadow-lg border-surface-400/20"
			in:fly={{ x: -300 }}
		>
			<div class="p-4 sm:p-8 space-y-4 transition-transform duration-300">
				<h2 class="h1 font-bold sm:h2">Login</h2>
				<!-- <form method="POST" action="?/login" class="space-y-4 py-4"> -->
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

				<!-- </form> -->
				<HLine width="w-full rounded-full mx-auto" thickness={2} />
				<div class="flex items-center justify-between">
					<p>
						<span>Don't have an Account?</span>
						<a
							class="anchor"
							href="/account/auth/login?register=true"
							on:click={() => (register = true)}>Register</a
						>
					</p>
					<a class="anchor" href="/account/auth/recover" on:click={() => (register = true)}
						>Forgot password?</a
					>
				</div>
			</div>
		</div>
	{:else}
		<div
			class="rounded-md overflow-hidden border-[1px] bg-surface-100 dark:variant-soft-surface shadow-lg border-surface-400/20"
			in:fly={{ x: 300 }}
		>
			<div class="p-4 sm:p-8 space-y-4 transition-transform duration-300">
				<h2 class="h1 font-bold sm:h2">Register</h2>

				<form method="POST" action="?/register" class="space-y-4 py-4">
					<Text bind:value={email} name="email" label="Email" />
					<Password label="Password" name="password1" />
					<Password label="Confirm password" name="password2" />
					<ActionButton icon={faUserCheck} text="Submit" variant="variant-filled-primary" />
				</form>
				<p>
					<span>Already have an Account?</span>
					<a class="anchor" href="/account/auth/login" on:click={() => (register = false)}>Login</a>
				</p>
			</div>
		</div>
	{/if}
</div>
