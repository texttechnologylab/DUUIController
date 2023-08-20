<script lang="ts">
	import { onMount } from 'svelte';

	export let name: string = 'Component';
	export let id: string;
	export let driver: string = 'Driver';
	export let description: string = 'Description';
	let status: string = '';

	onMount(() => {
		async function getStatus() {
			let response = await fetch('http://127.0.0.1:9090/status/' + id);
			let json = await response.json();
			status = json['message'];
		}

		getStatus();
	});
</script>

<div class="component">
	<div class="header">
		<p class="pipeline__component-title">{name}</p>
		<p class="pipeline__component-status">{status}</p>
	</div>
	<p class="pipeline__component-id">{id}</p>
	<p class="pipeline__component-driver">{driver}</p>
	<p class="pipeline__component-driver">{description}</p>
</div>

<style>
	.component {
		padding: 1rem 2rem;
		box-shadow: 0px 4px 12px 0px rgba(0, 0, 0, 0.25);
		display: flex;
		flex-direction: column;
		align-items: flex-start;
		gap: 1rem;
		border-radius: 0.25rem;
	}
	.header {
		display: flex;
		gap: 1rem;
	}

	.pipeline__component-status {
		margin-left: auto;
	}
</style>
