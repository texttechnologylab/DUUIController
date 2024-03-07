<script lang="ts">
	import { goto } from '$app/navigation'
	import { page } from '$app/stores'
	import { userSession } from '$lib/store'
	import Password from '$lib/svelte/components/Input/Password.svelte'
	import Text from '$lib/svelte/components/Input/TextInput.svelte'
	import { faGithub, faXTwitter } from '@fortawesome/free-brands-svg-icons'
	import { faGlobe } from '@fortawesome/free-solid-svg-icons'
	import Fa from 'svelte-fa'
	import { fly } from 'svelte/transition'

	let email: string = $page.url.searchParams.get('email') || ''
	let password: string

	let redirectTo: string = $page.url.searchParams.get('redirectTo') || '/account'

	let message: string
	$: message = $page.url.searchParams.get('message') ?? ''
	const emailPattern = new RegExp('[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+.[a-zA-Z]{2,}$')
	const loginUser = async () => {
		if (!emailPattern.test(email)) {
			message = 'Please enter a valid E-Mail address'
			return
		}
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

			goto(redirectTo)
		} else {
			if (response.status === 503) message = 'Could not reach the server. Try again later.'
			else message = result
		}
	}

	const login: boolean = true
</script>

<svelte:head>
	<title>Login</title>
</svelte:head>

<div class="grid md:flex items-center md:justify-center h-full md:m-16 w-full scroll-mt-4" id="top">
	<div class="grid relative md:grid-cols-2 section-wrapper lg:min-w-[900px] p-4 md:p-0">
		<div
			in:fly={{ x: 600, opacity: 100, duration: 800 }}
			class="space-y-8 md:p-16 md:px-8 relative"
		>
			<h2 class="h2 mb-16">Login</h2>
			{#if message}
				<p in:fly={{ y: -100 }} class="font-bold variant-soft-error p-4 rounded-md max-w-[40ch]">
					{message}
				</p>
			{/if}
			<div class="gap-8 flex flex-col">
				<div class="space-y-4">
					<Text bind:value={email} label="Email" name="email" />
					<Password
						bind:value={password}
						label="Password"
						name="password"
						on:keydown={(event) => {
							if (event.key === 'Enter') {
								loginUser()
							}
						}}
					/>
				</div>

				<a tabindex="-1" class="block mx-auto text-center anchor" href="/account/recover"
					>Forgot Password?
				</a>
				<button on:click={loginUser} class="variant-filled-primary button-modal self-center"
					>Sign In</button
				>
			</div>
			<p class="text-center">
				Don't have an Account?
				<a class="anchor" href="/account/register">Register</a>
			</p>
		</div>

		<div
			in:fly={{ x: 300, opacity: 100, duration: 800 }}
			class="hidden md:flex absolute top-0 w-1/2 transition-all duration-700 ease-in-out h-full
				   rounded-bl-[10%] bg-gradient-to-tr from-primary-500 to-primary-600 text-white
				   flex-col justify-center items-center gap-16 translate-x-full"
		>
			<h2 class="h2 text-3xl text-center max-w-[15ch]">
				{login ? 'Welcome Back' : 'Nice to meet you'}
			</h2>

			<div class="flex items-center gap-8 0">
				<a
					target="_blank"
					href="https://github.com/texttechnologylab"
					class="transition-opacity opacity-70 hover:opacity-100"
				>
					<Fa icon={faGithub} size="2x" />
				</a>
				<a
					target="_blank"
					href="https://twitter.com/ttlab_ffm"
					class="transition-opacity opacity-70 hover:opacity-100"
				>
					<Fa icon={faXTwitter} size="2x" />
				</a>
				<a
					target="_blank"
					href="https://www.texttechnologylab.org/"
					class="transition-opacity opacity-70 hover:opacity-100"
				>
					<Fa icon={faGlobe} size="2x" />
				</a>
			</div>
		</div>
	</div>
</div>
