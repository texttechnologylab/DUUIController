<script>
	import '../app.postcss';
	import Navbar from '../components/Navbar/Navbar.svelte';
	import './styles.css';
</script>

<div class="app min-h-full">
	<Navbar />
	<main class="mx-auto max-w-6xl p-4 md:p-8">
		<slot />
	</main>
</div>
