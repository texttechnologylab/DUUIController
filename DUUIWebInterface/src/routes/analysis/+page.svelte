<script lang="ts">
</script>

<svelte:head>
	<title>Analysis</title>
	<meta name="description" content="About this app" />
</svelte:head>

<div class="grid grid-cols-2 gap-4 items-center justify-center">
	<a
		class="p-8 border-2 border-slate-800 hover:bg-slate-800 hover:text-white
		flex items-center justify-center text-lg transition-colors"
		href="/analysis/new"
	>
		<p>Create new Pipeline</p>
	</a>

	<a
		class="p-8 border-2 border-slate-800 hover:bg-slate-800 hover:text-white
		flex items-center justify-center text-lg transition-colors"
		href="/templates"
	>
		<p>Load Template Pipeline</p>
	</a>
</div>
