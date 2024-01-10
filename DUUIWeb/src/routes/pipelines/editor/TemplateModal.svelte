<script lang="ts">
	import ActionButton from '$lib/svelte/widgets/action/ActionButton.svelte'
	import DriverIcon from '$lib/components/DriverIcon.svelte'
	import IconButton from '$lib/svelte/widgets/action/IconButton.svelte'
	import type { DUUIComponent } from '$lib/duui/component'
	import { Api, makeApiCall } from '$lib/duui/utils/api'
	import { includes } from '$lib/duui/utils/text'
	import { faCheck, faClose, faSearch, faTrash, faX } from '@fortawesome/free-solid-svg-icons'
	import { getModalStore } from '@skeletonlabs/skeleton'
	import { onMount } from 'svelte'
	import Fa from 'svelte-fa'
	import { v4 as uuidv4 } from 'uuid'
	import TextInput from '$lib/svelte/widgets/input/TextInput.svelte'

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
	<div class="w-modal-wide card items-start justify-start rounded-none shadow-lg container">
		<header class="flex flex-col">
			<div class="flex justify-between items-center p-4">
				<h3 class="h3">Templates</h3>
				<TextInput bind:value={searchText} icon={faSearch} placeholder="Search..." classes="hidden md:block"/>
				<IconButton icon={faClose} on:click={() => modalStore.close()} rounded="rounded-full" />
			</div>
		</header>
		<hr class="bg-surface-400/20 h-[1px] !border-0 rounded " />
		<div class="grid sm:grid-cols-2 md:grid-cols-3 gap-4 p-4">
			{#each filteredComponents as component (component.oid)}
				<button
					class="text-left p-4 hover:variant-glass bg-surface-100 dark:variant-soft-surface dark:hover:bg-surface-800 shadow-lg space-y-4 md:flex flex-col border-2
					{selectedComponents.includes(component) ? 'border-success-400' : 'border-transparent'}"
					on:click={() =>
						selectedComponents.includes(component) ? deselect(component) : select(component)}
				>
					<div class="md:flex items-center justify-between gap-4">
						<p class="text-lg font-bold break-words">{component.name}</p>
						<DriverIcon driver={component.settings.driver} />
					</div>
					<p class="grow">{component.description}</p>
					<div class="flex flex-wrap gap-2 p-2">
						{#each component.categories as category}
							<!-- svelte-ignore a11y-no-static-element-interactions -->
							<div class="chip variant-glass-primary">
								<span>
									{category}
								</span>
							</div>
						{/each}
					</div>
				</button>
			{/each}
		</div>
		<div>
			{#if filteredComponents.length === 0}
				<p class="py-32 text-center h3">No templates found</p>
			{/if}
		</div>

		<hr class="bg-surface-400/20 h-[1px] !border-0 rounded " />
		<footer class="flex flex-col">
			<div class="flex justify-between items-center shadow-lg p-4">
				<ActionButton
					text="Clear selection"
					icon={faClose}
					variant="variant-filled-error dark:variant-soft-error"
					_class={selectedComponents.length > 0 ? 'inline-flex' : 'hidden'}
					on:click={onClear}
				/>
				<ActionButton
					text="Confirm"
					icon={faCheck}
					variant="variant-filled-success dark:variant-soft-success"
					_class="ml-auto"
					on:click={onSelect}
				/>
			</div>
		</footer>
	</div>
{/if}
