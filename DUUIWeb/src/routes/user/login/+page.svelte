<script lang="ts">
	import { page } from '$app/stores'
	import {
		faArrowLeft,
		faArrowRight,
		faTriangleExclamation
	} from '@fortawesome/free-solid-svg-icons'
	import Fa from 'svelte-fa'
	import type { ActionData } from './$types'
	import { goto } from '$app/navigation'

	let email: string | null = $page.url.searchParams.has('email')
		? $page.url.searchParams.get('email')
		: ''
	export let form: ActionData

	export let data

	let { user } = data
	let message: string
	$: message = $page.url.searchParams.get('message') ?? ''
</script>

<svelte:head>
	<title>Login</title>
</svelte:head>

<div class="shadow-lg p-16 rounded-md card variant-form-material space-y-8">
	{#if message}
		<p class="variant-filled-error p-4 rounded-md shadow-lg">{message}</p>
	{/if}
	{#if form?.error}
		<p class="variant-filled-error p-4 rounded-md shadow-lg">{form.error}</p>
	{/if}
	<h2 class="h2">{user ? 'You are already logged in' : 'Login'}</h2>
	{#if user}
		<div class="flex justify-between gap-4">
			<button class="btn variant-ghost-primary flex gap-2" on:click={() => goto('/')}>
				<Fa icon={faArrowLeft} />
				<span>Go back</span>
			</button>
			<form method="POST" action="?/logout" class="space-y-4">
				<button
					type="submit"
					class="btn variant-filled-primary flex gap-2"
					on:click={() => goto('/')}
				>
					<span>Logout</span>
					<Fa icon={faArrowRight} />
				</button>
			</form>
		</div>
	{:else}
		<form method="POST" action="?/login" class="space-y-8">
			<label class="label">
				<span>E-Mail</span>
				<input
					class="input border-2 focus-within:outline-primary-400"
					type="text"
					name="email"
					value={email}
					required
				/>
			</label>
			<label class="label">
				<span>Password</span>
				<input
					class="input border-2 focus-within:outline-primary-400"
					type="password"
					name="password"
					required
				/>
			</label>
			<div class="flex justify-between items-center gap-8">
				<button type="submit" class="btn variant-filled-primary">Login</button>
			</div>
		</form>
		<div class="flex flex-col items-center gap-4">
			<div class="flex items-center justify-center space-x-4">
				<span>Don't have an account?</span><a href="/user/register" class="anchor"> Register</a>
			</div>
			<a href="/user/recover" class="anchor">Forgot Password?</a>
		</div>
	{/if}
</div>
