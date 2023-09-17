<script lang="ts">
	import { componentsStore } from '$lib/store.js'
	import {
		faBell,
		faEdit,
		faFilter,
		faFolderPlus,
		faNewspaper,
		faPlus,
		faRemove,
		faSearch
	} from '@fortawesome/free-solid-svg-icons'

	import Fa from 'svelte-fa'

	export let data

	let { pipelines } = data

	pipelines.forEach(({ components }) => {
		components.forEach((component) => {
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
			console.log(filteredPipelines)
		}
	}
</script>

<svelte:head>
	<title>Pipelines</title>
</svelte:head>
<div class="container h-full mx-auto flex flex-col space-y-4 md:space-y-8 my-16">
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
			<div class="card p-4 flex items-start variant-outline-primary relative">
				<div class="grid gap-4 grow items-center">
					<p class="h4">{pipeline.name}</p>
					<p>{pipeline.components.length} Component(s)</p>
				</div>

				<a class="btn-icon justify-self-end variant-glass-primary" href="/pipelines/{pipeline.id}">
					<span>
						<Fa size="lg" icon={faEdit} />
					</span>
				</a>
			</div>
		{/each}
	</div>
</div>

<!-- <Table source={tableSimple} interactive={true} on:selected={mySelectionHandler} /> -->
