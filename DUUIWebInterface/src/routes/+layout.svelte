<script>
	import '../app.postcss';
	import Sidebar from '../components/Sidebar/Sidebar.svelte';
	import './styles.css';
</script>

<div class="app">
	<Sidebar />
	<main>
		<slot />
	</main>
</div>

<style>
	.app {
		display: flex;
		align-items: center;
		min-height: 100vh;
	}

	main {
		margin: 0 auto;
		
	}
</style>
