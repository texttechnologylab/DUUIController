<script lang="ts">
	import PipelineComponent from '../components/Pipeline/PipelineComponent.svelte';
	import Button from '../components/button/Button.svelte';
	import { writable } from 'svelte/store';
	export let storeFE = writable({});

	let makeComponent = () => {
		components = [...components, 'A'];
	};

	let components: Array<string> = [];
	$storeFE = components;
</script>

<svelte:head>
	<title>Home</title>
	<meta name="description" content="Svelte demo app" />
</svelte:head>

<section>
	<h1>Home</h1>
	<Button text="Confirm" iconName="check-solid" on:click={makeComponent} />
	<Button text="Cancel" iconName="exclamation-circle-solid" error={true} />
	<div class="comps">
		{#each components as c}
			<PipelineComponent id={c} />
		{/each}
	</div>
</section>

<style>
	.comps {
		padding: 2rem 0rem;
		display: grid;
		grid-template-columns: 1fr 1fr;
		gap: 2rem;
	}
</style>
