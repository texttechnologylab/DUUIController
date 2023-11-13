<script lang="ts">
	import ActionButton from '$lib/svelte/widgets/action/ActionButton.svelte'
	import TextInput from '$lib/svelte/widgets/input/TextInput.svelte'
	import { faLockOpen } from '@fortawesome/free-solid-svg-icons'

	let email: string = ''
</script>

<svelte:head>
	<title>Recover</title>
</svelte:head>

<div class="card rounded-md shadow-lg p-4 space-y-8">
	<h2 class="h2">Forgot Password</h2>
	<form method="POST" class="space-y-8">
		<label class="label" for="email">
			<span>E-Mail</span>
			<TextInput name="email" id="email" bind:value={email} />
		</label>
		<ActionButton
			text="Request new password"
			icon={faLockOpen}
			variant="variant-filled-primary"
			disabled={email === ''}
		/>
	</form>
</div>
