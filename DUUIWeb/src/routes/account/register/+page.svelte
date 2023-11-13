<script lang="ts">
	import ActionButton from '$lib/svelte/widgets/action/ActionButton.svelte'
	import PasswordInput from '$lib/svelte/widgets/input/PasswordInput.svelte'
	import TextInput from '$lib/svelte/widgets/input/TextInput.svelte'
	import { faUserCheck } from '@fortawesome/free-solid-svg-icons'
	import type { ActionData } from './$types'

	export let form: ActionData
</script>

<svelte:head>
	<title>Register</title>
</svelte:head>

<div class="container variant-soft-surface shadow-lg p-4 md:p-8 space-y-4">
	{#if form?.error}
		<p class="variant-filled-error p-4 rounded-md shadow-lg">{form.error}</p>
	{/if}
	<h2 class="h2">Register</h2>
	<form method="POST" action="?/register" class="space-y-4">
		<label class="label" for="email">
			<span>E-Mail</span>
			<TextInput id="email" name="email" required={true} />
		</label>
		<label class="label" for="password1">
			<span>Password</span>
			<PasswordInput name="password1" required id="password1" />
		</label>
		<label class="label" for="password2">
			<span>Confirm Password</span>
			<PasswordInput name="password2" required id="password1" />
		</label>
		<div class="pt-8">
			<ActionButton icon={faUserCheck} text="Register" variant="variant-filled-primary" />
		</div>
	</form>
	<div class="flex items-center justify-center space-x-4">
		<span>Already have an account?</span><a href="/account/login" class="anchor"> Login</a>
	</div>
</div>
