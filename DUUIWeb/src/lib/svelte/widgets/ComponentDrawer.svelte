<script lang="ts">
	import { DUUIDrivers, type DUUIComponent } from '$lib/duui/component'
	import { errorToast, successToast } from '$lib/duui/utils/ui'
	import { currentPipelineStore } from '$lib/store'
	import { faCancel, faFileCircleCheck, faPlus, faTrash } from '@fortawesome/free-solid-svg-icons'
	import {
		getDrawerStore,
		getModalStore,
		getToastStore,
		type ModalSettings
	} from '@skeletonlabs/skeleton'
	import pkg from 'lodash'
	import { createEventDispatcher } from 'svelte'
	import Fa from 'svelte-fa'
	import { v4 as uuidv4 } from 'uuid'
	import Chips from './input/Chips.svelte'
	import ComponentOptions from './input/ComponentOptions.svelte'
	import Dropdown from './input/Dropdown.svelte'
	import JsonInput from './input/JsonInput.svelte'
	import TextArea from './input/TextArea.svelte'
	import TextInput from './input/TextInput.svelte'
	import { showModal } from '$lib/utils/modal'
	const { cloneDeep } = pkg

	const drawerStore = getDrawerStore()
	let component: DUUIComponent = $drawerStore.meta.component
	const inEditor = $drawerStore.meta.inEditor
	const creating = $drawerStore.meta.creating
	const modalStore = getModalStore()
	const toastStore = getToastStore()

	let parameters: Map<string, string> = new Map(Object.entries(component.parameters))
	const dispatcher = createEventDispatcher()

	const onUpdate = async () => {
		if (!inEditor) {
			component.parameters = Object.fromEntries(parameters)
			const response = await fetch('/api/components', {
				method: 'PUT',
				body: JSON.stringify(component)
			})

			if (response.ok) {
				toastStore.trigger(successToast('Component updated successfully'))
				parameters = new Map(Object.entries(component.parameters))
			}

			dispatcher('updated', { ...component })
			$currentPipelineStore.components[component.index] = component
		}

		drawerStore.close()
	}

	const onCreate = async () => {
		component.parameters = Object.fromEntries(parameters)
		const response = await fetch('/api/components', {
			method: 'POST',
			body: JSON.stringify({
				...component,
				index: $currentPipelineStore.components.length,
				pipeline_id: $currentPipelineStore.oid
			})
		})

		if (response.ok) {
			const result: DUUIComponent = await response.json()

			$currentPipelineStore.components = [
				...$currentPipelineStore.components,
				{ ...result, id: uuidv4() }
			]
		} else {
			errorToast(response.statusText)
		}
		drawerStore.close()
	}

	const onEdited = async () => {
		if (creating) {
			const copy = cloneDeep(component)
			copy.id = uuidv4()
			copy.index = $currentPipelineStore.components.length
			$currentPipelineStore.components = [...$currentPipelineStore.components, copy]

			// $currentPipelineStore.components.push({ ...component })
			// $currentPipelineStore.components = [...$currentPipelineStore.components, component]
		} else {
			$currentPipelineStore.components[component.index] = component
		}

		drawerStore.close()
	}

	const onRemove = async () => {
		const confirm = await showModal(
			{
				title: 'Remove Component',
				message: `Are you sure you want to remove ${component.name}?`,
				deleteText: 'Remove'
			},
			'deleteModal',
			modalStore
		)

		if (!confirm) return

		$currentPipelineStore.components = $currentPipelineStore.components.filter(
			(c) => c.id !== component.id
		)

		drawerStore.close()
	}

	const onDelete = async () => {
		const confirm = await showModal(
			{
				title: 'Delete Component',
				message: `Are you sure you want to delete ${component.name}?`
			},
			'deleteModal',
			modalStore
		)

		if (!confirm) return

		const response = await fetch('/api/components', {
			method: 'DELETE',
			body: JSON.stringify(component)
		})
		if (response.ok) {
			toastStore.trigger(successToast('Component deleted successfully'))

			$currentPipelineStore.components = $currentPipelineStore.components.filter(
				(c) => c.id !== component.id
			)

			dispatcher('deleteComponent', { oid: component.oid })
		} else {
			toastStore.trigger(errorToast('Error: ' + response.statusText))
		}

		drawerStore.close()
	}
</script>

<div class="menu-mobile">
	{#if creating}
		<button
			disabled={!component.driver || !component.name || !component.target}
			class="button-mobile"
			on:click={() => (inEditor ? onEdited() : onCreate())}
		>
			<Fa icon={faFileCircleCheck} />
			Confirm
		</button>
		<button type="button" class="button-mobile" on:click={drawerStore.close}>
			<Fa icon={faCancel} />
			Cancel
		</button>
	{:else}
		<button
			class="button-mobile"
			on:click={() => (inEditor ? onEdited() : onUpdate())}
			disabled={!component.driver || !component.name || !component.target}
		>
			<Fa icon={faFileCircleCheck} />
			Save
		</button>
		<button
			type="button"
			class="button-mobile"
			on:click={() => (inEditor ? onRemove() : onDelete())}
		>
			<Fa icon={faTrash} />
			Delete
		</button>
	{/if}
</div>

<div class="bg-surface-50-900-token h-screen relative">
	<div
		class="p-4 flex items-center gap-4 justify-between sticky top-0 z-10 bg-surface-100-800-token border-b border-color"
	>
		<h3 class="h3">{component.name}</h3>

		<div class="hidden md:block space-x-2">
			{#if creating}
				<button
					disabled={!component.driver || !component.name || !component.target}
					class="button-success ml-auto"
					on:click={() => (inEditor ? onEdited() : onCreate())}
				>
					<Fa icon={faFileCircleCheck} />
					Confirm
				</button>
				<button type="button" class="button-error" on:click={drawerStore.close}>
					<Fa icon={faCancel} />
					Cancel
				</button>
			{:else}
				<button
					class="button-success ml-auto"
					on:click={() => (inEditor ? onEdited() : onUpdate())}
					disabled={!component.driver || !component.name || !component.target}
				>
					<Fa icon={faFileCircleCheck} />
					Save
				</button>
				<button
					type="button"
					class="button-error"
					on:click={() => (inEditor ? onRemove() : onDelete())}
				>
					<Fa icon={faTrash} />
					{inEditor ? 'Remove' : 'Delete'}
				</button>
			{/if}
		</div>
	</div>

	<div class="space-y-8 p-4 bg-surface-50-900-token">
		<div class="space-y-4">
			<h4 class="h4">Properties</h4>

			<TextInput label="Name" name="name" bind:value={component.name} />
			<Dropdown label="Driver" name="driver" options={DUUIDrivers} bind:value={component.driver} />
			<TextInput style="md:col-span-2" label="Target" name="target" bind:value={component.target} />

			<Chips style="md:col-span-2" label="Tags" bind:values={component.tags} />
			<TextArea
				style="md:col-span-2"
				label="Description"
				name="description"
				bind:value={component.description}
			/>
			<!-- <TabGroup rounded="rounded-none" active="border-b-2 border-b-primary-500" border="border-none">
			<Tab name="Parameters" value={0} bind:group={tabSet}>Parameters</Tab>
			<Tab name="Options" value={1} bind:group={tabSet}>Options</Tab>
		</TabGroup> -->
		</div>

		<div class="space-y-4 border-t border-color pt-4">
			<h4 class="h4">Options</h4>
			<ComponentOptions {component} />
		</div>

		<div class="space-y-4 border-t border-color pt-4 pb-20">
			<h4 class="h4">Parameters</h4>
			<JsonInput bind:data={parameters} />
			<!-- <ComponentParameters bind:parameters={component.parameters} /> -->
		</div>
		<!-- <div class="md:col-span-2">
			{#if tabSet === 0}
				
			{:else if tabSet === 1}
				<JsonInput bind:data={options} />
			{/if}
		</div> -->
	</div>
</div>
