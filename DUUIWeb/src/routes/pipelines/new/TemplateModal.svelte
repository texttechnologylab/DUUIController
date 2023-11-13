<script lang="ts">
	import ActionButton from '$lib/svelte/widgets/action/ActionButton.svelte'
	import DriverIcon from '$lib/components/DriverIcon.svelte'
	import IconButton from '$lib/svelte/widgets/action/IconButton.svelte'
	import type { DUUIComponent } from '$lib/duui/component'
	import { Api, makeApiCall } from '$lib/utils/api'
	import { includes } from '$lib/utils/text'
	import { faCheck, faClose, faTrash, faX } from '@fortawesome/free-solid-svg-icons'
	import { getModalStore } from '@skeletonlabs/skeleton'
	import { onMount } from 'svelte'
	import Fa from 'svelte-fa'
	import { v4 as uuidv4 } from 'uuid'

	let components: DUUIComponent[] = []

	onMount(async () => {
		let response = await makeApiCall(Api.Templates, 'GET', {})
		let json = await response.json()
		components = json.components
	})

	let selectedComponents: DUUIComponent[] = []
	const modalStore = getModalStore()

	const onClear = () => {
		selectedComponents = []
	}
	// Handle Form Submission
	const onSelect = () => {
		if ($modalStore[0].response) $modalStore[0].response(selectedComponents)
		modalStore.close()
	}

	const select = (component: DUUIComponent) => {
		component.id = uuidv4()
		selectedComponents = [...selectedComponents, component]
	}

	const deselect = (component: DUUIComponent) => {
		selectedComponents = selectedComponents.filter((c) => c !== component)
	}

	let searchText: string = ''
	let filteredComponents = components

	$: {
		if (searchText === '') {
			filteredComponents = components
		} else {
			filteredComponents = components.filter((template) => {
				if (template.name.toLowerCase().includes(searchText.toLowerCase())) {
					return template
				}
			})
		}
	}
</script>

{#if $modalStore[0]}
	<div class="card items-start justify-start rounded-md shadow-lg container">
		<header class="flex flex-col">
			<div class="flex justify-between items-center shadow-lg p-4">
				<h3 class="h3">Templates</h3>
				<IconButton icon={faClose} on:click={() => modalStore.close()} rounded="rounded-full" />
			</div>
		</header>
		<div class="grid sm:grid-cols-2 md:grid-cols-3 xl:grid-cols-4 gap-4 p-4">
			{#each filteredComponents as component (component.oid)}
				<button
					class="text-left variant-soft-surface p-4 hover:shadow-lg space-y-4 flex flex-col border-2
					{selectedComponents.includes(component) ? 'border-success-400' : 'border-transparent'}"
					on:click={() =>
						selectedComponents.includes(component) ? deselect(component) : select(component)}
				>
					<div class="flex items-center justify-between gap-4">
						<p class="text-lg font-bold">{component.name}</p>

						{#if selectedComponents.includes(component)}
							<Fa icon={faCheck} size="lg" class="text-success-400" />
						{/if}
					</div>
					<p class="grow">{component.description}</p>
					<div class="flex items-center justify-between">
						<DriverIcon driver={component.settings.driver} />
					</div>
				</button>
			{/each}
		</div>

		<hr />
		<footer class="flex flex-col">
			<div class="flex justify-between items-center shadow-lg p-4">
				<ActionButton
					text="Clear selection"
					icon={faX}
					variant="variant-soft-error"
					_class={selectedComponents.length > 0 ? 'inline-flex' : 'hidden'}
					on:click={onClear}
				/>
				<ActionButton
					text="Confirm"
					icon={faCheck}
					variant="variant-soft-success"
					_class="ml-auto"
					on:click={onSelect}
				/>
			</div>
		</footer>
	</div>
{/if}
