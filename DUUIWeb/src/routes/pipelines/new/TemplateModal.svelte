<script lang="ts">
	import DriverIcon from '$lib/components/DriverIcon.svelte'
	import type { DUUIPipelineComponent } from '$lib/data'
	import { faCheck } from '@fortawesome/free-solid-svg-icons'
	import { getModalStore } from '@skeletonlabs/skeleton'
	import Fa from 'svelte-fa'

	export let components: DUUIPipelineComponent[]

	let selectedComponents: DUUIPipelineComponent[] = []
	const modalStore = getModalStore()

	// Handle Form Submission
	function onFormSubmit(): void {
		if ($modalStore[0].response) $modalStore[0].response(selectedComponents)
		modalStore.close()
	}

	function selectTemplate(component: DUUIPipelineComponent) {
		if (selectedComponents.includes(component)) {
			selectedComponents = selectedComponents.filter((c) => c !== component)
		} else {
			selectedComponents = [...selectedComponents, component]
		}
	}

	let searchText: string = ''
	let searchOpen: boolean = false
	let filteredTemplates = components

	$: {
		if (searchText === '') {
			filteredTemplates = components
		} else {
			filteredTemplates = components.filter((template) => {
				if (template.name.toLowerCase().includes(searchText.toLowerCase())) {
					return template
				}
			})
		}
	}
</script>

{#if $modalStore[0]}
	<div
		class="card max-w-7xl items-start justify-start overflow-y-scroll max-h-[90vh] md:max-h-[80vh] rounded-md shadow-lg"
	>
		<div class="flex justify-between items-center sticky top-0 bg-surface-800 shadow-lg p-4">
			<h3 class="h3">Select Template</h3>
			<button class="btn variant-filled-primary rounded-sm shadow-lg" on:click={onFormSubmit}
				>Choose</button
			>
		</div>
		<article class="grid md:grid-cols-2 lg:grid-cols-3 gap-8 p-8 cursor-pointer">
			{#each components as component (component.id)}
				<button
					class="flex flex-col rounded-md hover:shadow-2xl overflow-hidden card
					{selectedComponents.includes(component) ? 'border-2 border-primary-500' : ''}"
					on:click={() => selectTemplate(component)}
				>
					<div class="grid variant-soft-primary p-2 text-center">
						<p>{component.category}</p>
					</div>
					<header class=" p-4 flex items-center justify-between gap-4">
						<DriverIcon driver={component.settings.driver} />
						<p class="md:h4 text-left mr-auto">{component.name}</p>
					</header>
					<hr />
					<div class="p-4 text-left grow">
						<p class="text-sm md:text-base max-w-[40ch]">{component.description}</p>
					</div>
				</button>
			{/each}
		</article>
	</div>
{/if}
