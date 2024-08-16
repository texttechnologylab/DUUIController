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

	let email: string
	let password1: string
	let password2: string

	let login: boolean = false

	let message: string
	$: message = $page.url.searchParams.get('message') ?? ''
	const emailPattern = new RegExp('[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+.[a-zA-Z]{2,}$')
	const register = async () => {
		if (!emailPattern.test(email)) {
			message = 'Please enter a valid E-Mail address'
			return
		}

		if (!email || !password1 || !password2) {
			message = 'Please enter all required details.'
			return
		}

		const request = {
			method: 'POST',
			body: JSON.stringify({
				email: email,
				password1: password1,
				password2: password2
			}),
			headers: {
				'Content-Type': 'application/json'
			}
		}

		const response = await fetch('/auth/register', request)

		const result = await response.json()

		if (response.ok) {
			userSession.set(result.user)
			goto('/account')
		} else {
			message = result
		}
	}
</script>

<svelte:head>
	<title>Register</title>
</svelte:head>

<div class="grid md:flex items-center md:justify-center h-full md:m-16 w-full scroll-mt-4" id="top">
	<div class="grid relative md:grid-cols-2 section-wrapper lg:min-w-[900px] p-4 md:p-0">
		<div
			in:fly={{ x: -600, opacity: 100, duration: 800 }}
			class="relative space-y-8 md:p-16 md:px-8 md:col-start-2"
		>
			<h2 class="h2 mb-16">Register</h2>
			{#if message}
				<p in:fly={{ y: -100 }} class="font-bold variant-soft-error p-4 rounded-md max-w-[40ch]">
					{message}
				</p>
			{/if}
			<div class="flex flex-col gap-8">
				<div class="space-y-4">
					<Text bind:value={email} label="Email" name="email" required />
					<Password bind:value={password1} label="Password" name="password" required />
					<Password
						bind:value={password2}
						label="Repeat Password"
						name="password2"
						required
						on:keydown={(event) => {
							if (event.key === 'Enter') {
								register()
							}
						}}
					/>
				</div>
				<a tabindex="-1" class="block mx-auto text-center anchor" href="/account/recover"
					>Forgot Password?
				</a>
				<button
					on:click={register}
					class="button-primary button-modal self-center dark:!variant-filled-primary"
				>
					Sign Up
				</button>
			</div>
			<p class="text-center">
				Already have an Account?
				<a class="anchor" href="/account/login">Login</a>
			</p>
		</div>

		<div
			in:fly={{ x: -300, opacity: 100, duration: 800 }}
			class="hidden absolute top-0 w-1/2 transition-all duration-700 ease-in-out h-full rounded-br-[10%]
				   bg-gradient-to-tr from-primary-500 to-primary-600 text-white
				   md:flex flex-col justify-center items-center gap-16"
		>
			<h2 class="font-bold text-3xl text-center">
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
