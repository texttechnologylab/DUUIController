<script lang="ts">
	import { page } from '$app/stores'
	import { faTriangleExclamation } from '@fortawesome/free-solid-svg-icons'
	import Fa from 'svelte-fa'
	import type { ActionData } from './$types'

	export let form: ActionData
</script>

<svelte:head>
	<title>Register</title>
</svelte:head>

<div class="shadow-lg p-16 rounded-md card variant-form-material space-y-8">
	{#if form?.error}
		<p class="variant-filled-error p-4 rounded-md shadow-lg">{form.error}</p>
	{/if}
	<h2 class="h2">Register</h2>
	<form method="POST" action="?/register" class="space-y-8">
		<label class="label">
			<span>E-Mail</span>
			<input
				class="input border-2 focus-within:outline-primary-400"
				type="text"
				name="email"
				required
			/>
		</label>
		<label class="label">
			<span>Password</span>
			<input
				class="input border-2 focus-within:outline-primary-400"
				type="password"
				name="password1"
				required
			/>
		</label>
		<label class="label">
			<span>Confirm Password</span>
			<input
				class="input border-2 focus-within:outline-primary-400"
				type="password"
				name="password2"
				required
			/>
		</label>
		<button type="submit" class="btn variant-filled-primary">Register</button>
	</form>
	<div class="flex items-center justify-center space-x-4">
		<span>Already have an account?</span><a href="/user/login" class="anchor"> Login</a>
	</div>
</div>
