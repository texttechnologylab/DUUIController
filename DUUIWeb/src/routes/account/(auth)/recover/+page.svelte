<script lang="ts">
	import Text from '$lib/svelte/components/Input/TextInput.svelte'
	import { faEnvelope, faEnvelopeCircleCheck } from '@fortawesome/free-solid-svg-icons'
	import Fa from 'svelte-fa'
	import { fly } from 'svelte/transition'

	let email: string
	let recoverAddress: string = ''
	let message: string = ''

	const emailPattern = new RegExp('[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+.[a-zA-Z]{2,}$')

	const recover = async () => {
		if (!emailPattern.test(recoverAddress)) {
			message = 'Please enter a valid E-Mail address'
			return
		}
		const response = await fetch(`/auth/recover`, {
			method: 'POST',
			body: JSON.stringify({ email: recoverAddress })
		})

		const result = await response.json()

		if (response.ok) {
			email = recoverAddress
		} else {
			message = result
		}
	}
</script>

<svelte:head>
	<title>Recover</title>
</svelte:head>

<div class="container max-w-4xl mx-auto h-full flex items-center">
	<div>
		{#if email}
			<div class="section-wrapper p-8 space-y-2 max-w-[60ch]">
				<p>
					An email has been sent to <span class="font-bold">{email}</span>.
				</p>
				<p>Check your inbox to reset your password.</p>
				<Fa icon={faEnvelopeCircleCheck} size="lg" />
			</div>
		{:else}
			<div class="section-wrapper p-8 space-y-16 scroll-mt-4" id="top">
				<div class="space-y-8">
					<h2 class="h2">Recover Password</h2>
					<p class="max-w-[60ch]">
						Enter the E-Mail of your account below and we will send you a link to update your
						password.
					</p>
				</div>
				<div class="space-y-4 relative">
					{#if message}
						<p in:fly={{ y: 10 }} class=" font-bold variant-soft-error p-4 rounded-md max-w-[40ch]">
							{message}
						</p>
					{/if}
					<Text label="Email" name="email" bind:value={recoverAddress} />
					<button
						class="button-primary button-modal self-center dark:!variant-filled-primary"
						on:click={recover}
					>
						<Fa icon={faEnvelope} size="lg" />
						<span>Reset Password</span>
					</button>
				</div>
			</div>
		{/if}
	</div>
</div>
