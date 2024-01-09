<script lang="ts">
	import ActionButton from '$lib/svelte/widgets/action/ActionButton.svelte'
	import Password from '$lib/svelte/widgets/input/Password.svelte'
	import { faExclamationTriangle, faUserCheck } from '@fortawesome/free-solid-svg-icons'
	import { fly } from 'svelte/transition'
	import Text from '$lib/svelte/widgets/input/TextInput.svelte'
	import Link from '$lib/svelte/components/Link.svelte'
	import Fa from 'svelte-fa'
	import { page } from '$app/stores'
	import type { ActionData } from '../../auth/login/$types'
	import { userSession } from '$lib/store'
	import { goto } from '$app/navigation'

	let email: string
	let password1: string
	let password2: string

	export let form: ActionData

	let message: string
	$: message = $page.url.searchParams.get('message') ?? ''

	const register = async () => {
		const response = await fetch('/auth/register', {
			method: 'POST',
			body: JSON.stringify({
				email: email,
				password1: password1,
				password2: password2
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
	<title>Register</title>
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
		class="rounded-md overflow-hidden border-[1px] bg-surface-100 dark:variant-soft-surface shadow-lg border-surface-400/20"
		in:fly={{ x: 300 }}
	>
		<div class="p-4 sm:p-8 space-y-4 transition-transform duration-300">
			<h2 class="h1 font-bold sm:h2">Register</h2>

			<div class="space-y-4 py-4">
				<Text bind:value={email} label="Email" />
				<Password bind:value={password1} label="Password" />
				<Password bind:value={password2} label="Confirm password" />
				<ActionButton
					icon={faUserCheck}
					text="Confirm"
					variant="variant-filled-primary"
					on:click={register}
				/>
			</div>
			<p>
				<span>Already have an Account?</span>
				<Link href="/account/login" underline={true}>Login</Link>
			</p>
		</div>
	</div>
</div>
