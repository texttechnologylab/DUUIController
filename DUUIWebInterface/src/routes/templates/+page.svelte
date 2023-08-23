<script lang="ts">
	import type { Template } from '../../Interfaces/interfaces';
	import TemplateElement from '../../components/TemplateElement.svelte';
	

	let template: Template = {
		displayName: 'Find Global Names and all Paragraphs',
		createdAt: new Date(),
		timesUsed: 0,
		components: [

		]
	};
</script>

<svelte:head>
	<title>Templates</title>
	<meta name="description" content="About this app" />
</svelte:head>

<h1 class="my-4 text-4xl text-center">Templates</h1>
<div class="grid gap-8 grid-cols-2 p-8">
	<TemplateElement {template} />
</div>
