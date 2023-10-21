<script lang="ts">
	import type { DUUIComponent } from '$lib/duui/component.js'
	import { componentsStore } from '$lib/store.js'
	import { faEdit, faPlus, faSearch } from '@fortawesome/free-solid-svg-icons'

	import Fa from 'svelte-fa'

	export let data

	let { pipelines } = data

	pipelines.forEach(({ components }) => {
		components.forEach((component: DUUIComponent) => {
			$componentsStore = [...$componentsStore, component]
		})
	})

	let searchText: string = ''
	let searchOpen: boolean = false
	let filteredPipelines = pipelines

	$: {
		if (searchText === '') {
			filteredPipelines = pipelines
		} else {
			filteredPipelines = pipelines.filter((pipeline) => {
				if (pipeline.name.toLowerCase().includes(searchText.toLowerCase())) {
					return pipeline
				}
			})
		}
	}
</script>

<svelte:head>
	<title>Pipelines</title>
</svelte:head>
<div class="container h-full mx-auto flex flex-col space-y-4 md:space-y-8">
	<div class="flex gap-4">
		<a href="pipelines/new" class="btn variant-filled-primary shadow-lg">
			<span>Create</span>
			<Fa icon={faPlus} />
		</a>
		<div class="flex items-center justify-start card rounded-sm shadow-lg">
			<button
				class="btn-icon variant-filled-primary p-4 rounded-sm"
				on:click={() => (searchOpen = !searchOpen)}
			>
				<Fa icon={faSearch} />
			</button>
			{#if searchOpen}
				<input class="input" type="text" placeholder="search..." bind:value={searchText} />
			{/if}
		</div>
	</div>

	<div class="grid gap-4 md:gap-8 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 relative">
		{#each filteredPipelines as pipeline}
			<a
				class="card rounded-md hover:shadow-lg focus-within:shadow-lg p-4 flex items-start relative"
				href="/pipelines/{pipeline.id}"
			>
				<div class="grid gap-4 grow items-center">
					<p class="h4 break-words">{pipeline.name}</p>
					<p>{pipeline.components.length} Component(s)</p>
				</div>
			</a>
		{/each}
	</div>
</div>

<!-- <Table source={tableSimple} interactive={true} on:selected={mySelectionHandler} /> -->
