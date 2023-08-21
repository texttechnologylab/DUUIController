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

