<script lang="ts">
	import { page } from '$app/stores'
	import ActionButton from '$lib/svelte/widgets/action/ActionButton.svelte'
	import Text from '$lib/svelte/widgets/input/TextInput.svelte'
	import {
		faEnvelope,
		faEnvelopeCircleCheck,
		faExclamationTriangle
	} from '@fortawesome/free-solid-svg-icons'
	import Fa from 'svelte-fa'
	import type { ActionData } from '../../auth/login/$types'

	let email: string = $page.url.searchParams.get('email') || ''
	let recoverAddress: string = ''
	export let form: ActionData
</script>

<svelte:head>
	<title>Recover</title>
</svelte:head>

<div class="space-y-4 py-4 w-modal">
	{#if email}
		<div class="flex items-start justify-between variant-filled-primary p-4 gap-4 shadow-lg">
			<p>
				An email has been sent to <span class="underline">{email}</span>. Check your inbox to reset
				your password.
			</p>
			<Fa icon={faEnvelopeCircleCheck} size="lg" />
		</div>
	{/if}
	{#if form?.error}
		<div class="flex items-center justify-between variant-filled-error p-4">
			<p>{form.error}</p>
			<Fa icon={faExclamationTriangle} size="lg" />
		</div>
	{/if}
	{#if !email}
		<div
			class="border-[1px] bg-surface-100 dark:variant-soft-surface shadow-lg border-surface-400/20 p-4 sm:p-8 space-y-4"
		>
			<header class="space-y-8">
				<h2 class="h2">Recover Password</h2>
				<p>
					Enter the E-Mail of your account below and we will send you a link to update your
					password.
				</p>
			</header>
			<form action="?/reset" method="POST" class="space-y-4">
				<Text label="Email" name="email" bind:value={recoverAddress} />
				<ActionButton
					text="Request new password"
					icon={faEnvelope}
					variant="variant-filled-primary dark:variant-soft-primary"
					disabled={recoverAddress === ''}
				/>
			</form>
		</div>
	{/if}
</div>
