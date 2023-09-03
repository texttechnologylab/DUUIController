<script>
	import '../app.postcss';
	import '../app.css';
	import Footer from '../components/Footer.svelte';
	import Navbar from '../components/Navbar/Navbar.svelte';
	import Toast from '../components/Toast.svelte';
</script>

<Navbar />
<div class="flex flex-col min-h-full">
	<main>
		<slot />
	</main>
	<Footer />
</div>
<Toast />
