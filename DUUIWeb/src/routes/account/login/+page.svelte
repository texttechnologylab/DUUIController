<script lang="ts">
	import { page } from '$app/stores'
	import {
		faArrowLeft,
		faArrowRightFromBracket,
		faArrowRightToBracket,
		faExclamationTriangle,
		faUserCheck,
		faUserPlus
	} from '@fortawesome/free-solid-svg-icons'
	import type { ActionData } from './$types'
	import ActionButton from '$lib/svelte/widgets/action/ActionButton.svelte'
	import TextInput from '$lib/svelte/widgets/input/TextInput.svelte'
	import PasswordInput from '$lib/svelte/widgets/input/PasswordInput.svelte'
	import Fa from 'svelte-fa'
	import { goto } from '$app/navigation'

	let email: string = $page.url.searchParams.get('email') || ''
	export let form: ActionData

	export let data

	let { user } = data

	let register: boolean = $page.url.searchParams.get('register') === 'true' || false

	let message: string
	$: message = $page.url.searchParams.get('message') ?? ''
</script>

<svelte:head>
	<title>Login</title>
</svelte:head>

<div class="space-y-4 py-4 self-stretch grow sm:grow-0">
	{#if message}
		<div class="flex items-center justify-between variant-filled-warning p-4 shadow-lg">
			<p>{message}</p>
			<Fa icon={faExclamationTriangle} size="lg" />
		</div>
	{/if}
	{#if form?.error}
		<div class="flex items-center justify-between variant-filled-error p-4">
			<p>{form.error}</p>
			<Fa icon={faExclamationTriangle} size="lg" />
		</div>
	{/if}

	<div class="container dark:bg-surface-800 shadow-lg grid sm:grid-cols-2">
		<!-- Login -->
		<div class="p-4 sm:p-8 space-y-4 transition-transform duration-300">
			{#if !register}
				<h2 class="h1 sm:h2">Login</h2>
				<form method="POST" action="?/login" class="space-y-4 py-4">
					<TextInput bind:value={email} name="email" id="email" />
					<PasswordInput id="password" name="password" />
					<ActionButton
						icon={faArrowRightToBracket}
						text="Login"
						variant="variant-filled-primary"
					/>
				</form>
			{:else}
				<div class="space-y-8 grid">
					<h2 class="h3 max-w-[15ch]">Already have an Account?</h2>
					<ActionButton
						text="Login"
						icon={faArrowRightToBracket}
						on:click={() => {
							goto('/account/login')
							register = false
						}}
					/>
				</div>
			{/if}
		</div>

		<!-- Register -->
		<div
			class="{register
				? 'row-start-1 border-b-2 sm:border-r-2 sm:border-b-0 border-r-surface-500 border-b-surface-500'
				: 'border-t-2 sm:border-l-2 sm:border-t-0 border-l-surface-500 border-t-surface-500'} transition-transform duration-300 p-4 sm:p-8 space-y-4"
		>
			{#if register}
				<h2 class="h1 sm:h2">Register</h2>
				<form method="POST" action="?/login" class="space-y-4 py-4">
					<TextInput bind:value={email} name="email" id="email" />
					<PasswordInput id="password1" name="password" />
					<PasswordInput id="password2" name="repeat password" />
					<ActionButton icon={faUserCheck} text="Submit" variant="variant-filled-primary" />
				</form>
			{:else}
				<div class="space-y-8 grid">
					<h2 class="h3 max-w-[15ch]">Don't have an Account?</h2>
					<ActionButton
						text="Register"
						icon={faUserPlus}
						on:click={() => {
							goto('/account/login?register=true')
							register = true
						}}
					/>
				</div>
			{/if}
		</div>
	</div>
	<!-- <div class="container variant-soft-surface shadow-lg p-4 sm:p-8 space-y-8 grid grid-cols-2">
		<h2 class="h2">{user ? 'You are already logged in' : 'Login'}</h2>
		{#if user}
			<div class="flex justify-between gap-4">
				<ActionButton
					text="Back"
					icon={faArrowLeft}
					variant="variant-filled-primary"
					on:click={() => goto('/')}
				/>

				<form method="POST" action="?/logout" class="space-y-4">
					<ActionButton
						leftToRight={false}
						text="Logout"
						icon={faArrowRightFromBracket}
						variant="variant-filled-primary"
					/>
				</form>
			</div>
		{:else}
			<form method="POST" action="?/login" class="space-y-4">
				<label class="label" for="email">
					<span>E-Mail</span>
					<TextInput bind:value={email} name="email" id="email" />
				</label>
				<label class="label" for="password">
					<span>Password</span>
					<PasswordInput id="password" name="password" />
				</label>
				<div class="pt-8">
					<ActionButton icon={faUserCheck} text="Login" variant="variant-filled-primary" />
				</div>
			</form>
			<div class="flex flex-col items-center gap-4">
				<div class="flex items-center justify-center space-x-4">
					<span>Don't have an account?</span><a href="/account/register" class="anchor">
						Register</a
					>
				</div>
				<a href="/account/recover" class="anchor">Forgot Password?</a>
			</div>
		{/if}
	</div> -->
</div>
