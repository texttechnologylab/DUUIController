<script lang="ts">
	import ActionButton from './ActionButton.svelte'
	import { DUUIDrivers, type DUUIComponent } from '$lib/duui/component'
	import { getModalStore, getToastStore, type ModalSettings } from '@skeletonlabs/skeleton'
	import { isEqual } from 'lodash'

	import IconButton from './IconButton.svelte'
	import { faClose, faFilePen, faTrash } from '@fortawesome/free-solid-svg-icons'
	import { toTitleCase } from '$lib/utils/text'
	import { createEventDispatcher } from 'svelte'
	import { currentPipelineStore, markedForDeletionStore } from '$lib/store'
	import { success } from '$lib/utils/ui'
	import { makeApiCall, Api } from '$lib/utils/api'

	const dispatcher = createEventDispatcher()
	const modalStore = getModalStore()
	let component: DUUIComponent = $modalStore[0].meta.component

	let hasChanges: boolean = false
	let name: string = component.name
	let description: string = component.description
	let driver: string = component.settings.driver
	let target: string = component.settings.target

	let parameters: Map<string, string>
	let options: Map<string, string>

	let categories: string[] = [...component.categories]
	let chipText: string = ''

	const toastStore = getToastStore()

	const deleteComponent = () => {
		$currentPipelineStore.components = $currentPipelineStore.components.filter(
			(c) => c.oid !== component.oid
		)
		modalStore.close()
	}

	const addCategory = () => {
		if (chipText.length > 0 && !categories.includes(chipText)) {
			categories = [...categories, toTitleCase(chipText)]
		}
		chipText = ''
	}

	const removeCategory = (category: string) => {
		categories = categories.filter((c) => c !== category)
	}

	const discardChanges = () => {
		component.name = name
		component.description = description
		component.settings.driver = driver
		component.settings.target = target
	}

	let onDelete = () => {
		new Promise<boolean>((resolve) => {
			const modal: ModalSettings = {
				type: 'confirm',
				title: 'Delete Component',
				buttonTextConfirm: 'Delete',
				body: `Are you sure you want to delete ${component.name}?`,
				response: (r: boolean) => {
					resolve(r)
				}
			}
			modalStore.trigger(modal)
		}).then(async (accepted: boolean) => {
			if (!accepted) return

			$markedForDeletionStore.push(component.oid)

			// let response = await makeApiCall(Api.Pipelines, 'DELETE', {
			// 	oid: component.oid,
			// 	component: true
			// })

			toastStore.trigger(success('Component deleted successfully'))
			dispatcher('delete', { component: component })
		})
	}

	const updateComponent = async () => {
		let response = await makeApiCall(Api.Components, 'PUT', component)
		if (response.ok) {
			toastStore.trigger(success('Changes saved successfully'))
			discardChanges()
		}
	}

	$: {
		hasChanges =
			name !== component.name ||
			description !== component.description ||
			driver !== component.settings.driver ||
			target !== component.settings.target

		if (parameters) {
			hasChanges ||= !isEqual(Object.fromEntries(parameters), component.settings.parameters)
		}

		if (options) {
			hasChanges ||= !isEqual(Object.fromEntries(options), component.settings.options)
		}
	}
</script>

{#if $modalStore[0]}
	<div class="card items-start justify-start rounded-md shadow-lg container">
		<header class="flex flex-col">
			<div class="flex justify-between items-center shadow-lg p-4">
				<h3 class="h3">{component.name}</h3>
				<IconButton icon={faClose} on:click={() => modalStore.close()} rounded="rounded-full" />
			</div>
		</header>

		<div class="grid sm:grid-cols-2 xl:grid-cols-4 gap-4 p-4">
			<label class="label">
				<span>Name</span>
				<input class="border-2 input" type="text" bind:value={name} />
			</label>

			<label class="label">
				<span>Driver</span>
				<select class="border-2 select input" bind:value={driver}>
					{#each DUUIDrivers as driver}
						<option value={driver}>{driver}</option>
					{/each}
				</select>
			</label>
			<label class="label col-span-2">
				<span>Target</span>
				<input class="border-2 input" type="text" bind:value={target} />
			</label>
			<label
				class="col-span-2 flex items-center flex-wrap gap-2 p-4 variant-soft-surface border-2 border-surface-500 rounded-md"
			>
				{#each categories as category}
					<!-- svelte-ignore a11y-no-static-element-interactions -->
					<span class="chip variant-filled" on:click={() => removeCategory(category)} on:keypress
						>{category}</span
					>
				{/each}
				<input
					class="border-none appearance-none"
					type="text"
					bind:value={chipText}
					on:keypress={(event) => {
						if (event.key === 'Enter') {
							addCategory()
						}
					}}
				/>
			</label>
		</div>
		<hr />

		<footer class="flex flex-col">
			<div class="flex justify-between items-center shadow-lg p-4">
				<ActionButton text="Update" icon={faFilePen} on:click={updateComponent} />
				<ActionButton
					text="Delete"
					icon={faTrash}
					variant="variant-soft-error"
					on:click={deleteComponent}
				/>
			</div>
		</footer>
	</div>
{/if}
