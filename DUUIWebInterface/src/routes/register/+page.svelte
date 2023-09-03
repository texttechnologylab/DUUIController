<script>
	import PasswordInput from '../../components/Forms/PasswordInput.svelte';
	import TextInput from '../../components/Forms/TextInput.svelte';

	const validate = () => {
		console.log('Validated form.');
	};
</script>

<svelte:head>
	<title>Login</title>
	<meta name="description" content="Login to DUUI" />
</svelte:head>

<div class="flex mx-auto flex-col gap-4 shadow-2xl px-4 py-8 border-2 border-gray-400 max-w-md">
	<h1 class="text-center text-4xl">Login</h1>
	<form class="flex flex-col gap-4 p-8">
		<TextInput label={'E-Mail'} />
		<PasswordInput label={'Password'} />
		<button
			class="px-4 py-3 hover:bg-blue-600 transition-colors duration-300 rounded-full text-white font-bold bg-blue-500 mt-8"
			type="submit"
		>
			Register
		</button>
	</form>
	<div class="flex flex-col gap-4 items-center">
		<a href="/reset-password" class="underline text-blue-500">Forgot Password?</a>
		<p>
			Already have an account?
			<a href="/login" class="underline text-blue-500">Login</a>
		</p>
	</div>
</div>
