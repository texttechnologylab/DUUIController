<script lang="ts">
	import DriverIcon from '$lib/components/DriverIcon.svelte'
	import type { DUUIPipelineComponent } from '$lib/data'
	import { faCheck, faX } from '@fortawesome/free-solid-svg-icons'
	import { getModalStore } from '@skeletonlabs/skeleton'
	import { onMount } from 'svelte'
	import Fa from 'svelte-fa'

	let components: DUUIPipelineComponent[] = []

	onMount(async () => {
		const response = await fetch('/pipelines/api/components', {
			method: 'GET',
			headers: {
				'Content-Type': 'application/json'
			}
		})

		let object = await response.json()
		components = object.components
	})

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
		class="card max-w-7xl items-start justify-start overflow-y-scroll max-h-[90vh] md:max-h-[80vh] overflow-hidden rounded-md shadow-lg"
	>
		<div class="sticky top-0 flex flex-col card z-10">
			<div class="flex justify-between items-center shadow-lg p-4">
				<h3 class="h3">Select Templates</h3>
				<button class="btn-icon shadow-lg variant-glass-success" on:click={onFormSubmit}>
					<Fa icon={faCheck} />
				</button>
			</div>
		</div>
		<article class="grid md:grid-cols-2 lg:grid-cols-3 gap-4 p-4 cursor-pointer">
			{#each components as component (component.id)}
				<button
					class="flex flex-col rounded-md hover:shadow-2xl overflow-hidden card
					{selectedComponents.includes(component) ? 'border-2 border-success-500' : ''}"
					on:click={() => selectTemplate(component)}
				>
					<!--  -->

					<p class="variant-soft-primary p-2 grid text-center">{component.category}</p>

					<header class="p-4 flex items-center justify-between gap-4">
						<DriverIcon driver={component.settings.driver} />
						<p class="md:h4 text-left mr-auto">{component.name}</p>
					</header>
					<hr />
					<p class="text-sm text-left p-4 md:text-base max-w-[40ch]">{component.description}</p>
				</button>
			{/each}
		</article>
	</div>
{/if}
