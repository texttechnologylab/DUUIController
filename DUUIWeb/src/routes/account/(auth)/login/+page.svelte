<script lang="ts">
	import { goto } from '$app/navigation'
	import { page } from '$app/stores'
	import { userSession } from '$lib/store'
	import Password from '$lib/svelte/widgets/input/Password.svelte'
	import { faGlobe } from '@fortawesome/free-solid-svg-icons'
	import Fa from 'svelte-fa'
	import { fly } from 'svelte/transition'
	import Text from '$lib/svelte/widgets/input/TextInput.svelte'
	import { faGithub, faXTwitter } from '@fortawesome/free-brands-svg-icons'
	import { onMount } from 'svelte'
	import { scrollIntoView } from '$lib/duui/utils/ui'

	let email: string = $page.url.searchParams.get('email') || ''
	let password: string
	let password2: string

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

	onMount(() => {
		scrollIntoView('top')
	})
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
			{#if message}
				<p in:fly={{ y: -100 }} class="font-bold variant-filled-error p-4 rounded-md max-w-[40ch]">
					{message}
				</p>
			{/if}
			<h2 class="h2 font-bold mb-16">Login</h2>
			<div class="gap-8 flex flex-col">
				<div class="space-y-4">
					<Text bind:value={email} label="Email" name="email" />
					<Password bind:value={password} label="Password" name="password" />
				</div>

				<a class="block text-center anchor text-sm" href="/account/recover">Forgot Password? </a>
				<button
					on:click={loginUser}
					class="button-primary button-modal uppercase tracking-widest self-center">sign in</button
				>
			</div>
			<a href="/account/register" class="block">
				Don't have an Account?
				<p class="anchor inline">Register</p>
			</a>
		</div>
		<div class="space-y-8 md:p-16 md:px-8 col-start-2 hidden md:invisible">
			<h2 class="h2 font-bold mb-16">Register</h2>
			<div class="flex flex-col gap-8">
				<div class="space-y-4">
					<Text disabled={true} bind:value={email} label="Email" name="email" />
					<Password disabled={true} bind:value={password} label="Password" name="password" />
					<Password
						disabled={true}
						bind:value={password2}
						label="Repeat Password"
						name="password2"
					/>
				</div>
				<a class="block text-center anchor text-sm" href="/account/recover">Forgot Password? </a>
				<button class="button-primary button-modal uppercase tracking-widest self-center">
					sign up
				</button>
			</div>
			<a href="/account/login" class="block">
				Already have an Account?
				<p class="anchor inline">Login</p>
			</a>
		</div>

		<div
			in:fly={{ x: 300, opacity: 100, duration: 800 }}
			class="hidden md:flex absolute top-0 w-1/2 transition-all duration-700 ease-in-out h-full rounded-bl-[10%]
				   bg-gradient-to-tr from-primary-500 to-primary-600 text-white
				    flex-col justify-center items-center gap-16 translate-x-full"
		>
			<h2 class="h2 font-bold text-3xl text-center max-w-[15ch]">
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
