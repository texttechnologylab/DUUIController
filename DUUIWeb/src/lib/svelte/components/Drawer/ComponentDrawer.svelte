<script lang="ts">
	import { DUUIDrivers, type DUUIComponent } from '$lib/duui/component'
	import { errorToast, successToast } from '$lib/duui/utils/ui'
	import { currentPipelineStore, exampleComponent } from '$lib/store'
	import { showConfirmationModal } from '$lib/svelte/utils/modal'
	import {
		faAngleDoubleRight,
		faCancel,
		faFileCircleCheck,
		faInfo,
		faTrash
	} from '@fortawesome/free-solid-svg-icons'
	import { getDrawerStore, getModalStore, getToastStore } from '@skeletonlabs/skeleton'
	import pkg from 'lodash'
	import { createEventDispatcher } from 'svelte'
	import Fa from 'svelte-fa'
	import { v4 as uuidv4 } from 'uuid'
	import DriverIcon from '../DriverIcon.svelte'
	import Chips from '../Input/Chips.svelte'
	import ComponentOptions from '../Input/ComponentOptions.svelte'
	import Dropdown from '../Input/Dropdown.svelte'
	import JsonInput from '../Input/JsonInput.svelte'
	import TextArea from '../Input/TextArea.svelte'
	import TextInput from '../Input/TextInput.svelte'
	import Popup from '../Popup.svelte'
	const { cloneDeep } = pkg

	const drawerStore = getDrawerStore()
	let component: DUUIComponent = $drawerStore.meta.component
	const inEditor = $drawerStore.meta.inEditor
	const creating = $drawerStore.meta.creating
	const example = $drawerStore.meta.example

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
			if (component.pipeline_id) {
				$currentPipelineStore.components.forEach((c, index) => (c.index = index))
				component.id = uuidv4()
				$currentPipelineStore.components[component.index] = component
			}
		}

		drawerStore.close()
	}

	const onCreate = async () => {
		if (example) {
			$exampleComponent = { ...component }
			$currentPipelineStore.components[component.index] = $exampleComponent
			$currentPipelineStore.components = $currentPipelineStore.components
		} else {
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
		}

		drawerStore.close()
	}

	const onEdited = async () => {
		if (creating) {
			const copy = cloneDeep(component)
			copy.id = uuidv4()
			copy.index = $currentPipelineStore.components.length
			$currentPipelineStore.components = [...$currentPipelineStore.components, copy]
		} else {
			$currentPipelineStore.components[component.index] = component
		}

		drawerStore.close()
	}

	const onRemove = async () => {
		const confirm = await showConfirmationModal(
			{
				title: 'Remove Component',
				message: `Are you sure you want to remove ${component.name}?`,
				textYes: 'Remove'
			},
			modalStore
		)

		if (!confirm) return

		$currentPipelineStore.components = $currentPipelineStore.components.filter(
			(c) => c.id !== component.id
		)

		drawerStore.close()
	}

	const onDelete = async () => {
		const confirm = await showConfirmationModal(
			{
				title: 'Delete Component',
				message: `Are you sure you want to delete ${component.name}?`,
				textYes: 'Delete'
			},
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

<div class="menu-mobile lg:!max-w-[50%]">
	<button class="button-mobile" on:click={drawerStore.close}>
		<Fa icon={faAngleDoubleRight} size="lg" />
		<span>Close</span>
	</button>

	{#if example}
		<button
			disabled={!component.driver || !component.name || !component.target}
			class="button-mobile"
			on:click={onCreate}
		>
			<Fa icon={faFileCircleCheck} />
			Save
		</button>
		<button type="button" class="button-mobile" on:click={onRemove}>
			<Fa icon={faTrash} />
			Remove
		</button>
	{:else if creating}
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
		class="p-4 flex-center-4 justify-between sticky top-0 z-10 bg-surface-100-800-token border-b border-color"
	>
		<div class="flex-center-4">
			<!-- <button
				class="!hidden md:!inline-flex button-neutral !rounded-full aspect-square !p-3"
				on:click={drawerStore.close}
			>
				<Fa icon={faAngleDoubleRight} size="lg" />
			</button> -->
			<div class="flex-center-4">
				<DriverIcon driver={component.driver} />
				<h3 class="h3">{component.name}</h3>
			</div>
		</div>

		<div class="hidden md:block space-x-2">
			{#if example}
				<button
					disabled={!component.driver || !component.name || !component.target}
					class="button-neutral"
					on:click={onCreate}
				>
					<Fa icon={faFileCircleCheck} />
					<span>Save</span>
				</button>
				<button type="button" class="button-neutral" on:click={onRemove}>
					<Fa icon={faTrash} />
					<span>Remove</span>
				</button>
			{:else if creating}
				<button
					disabled={!component.driver || !component.name || !component.target}
					class="button-neutral ml-auto"
					on:click={() => (inEditor ? onEdited() : onCreate())}
				>
					<Fa icon={faFileCircleCheck} />
					Confirm
				</button>
				<button type="button" class="button-neutral" on:click={drawerStore.close}>
					<Fa icon={faCancel} />
					Cancel
				</button>
			{:else}
				<button
					class="button-neutral ml-auto"
					on:click={() => (inEditor ? onEdited() : onUpdate())}
					disabled={!component.driver || !component.name || !component.target}
				>
					<Fa icon={faFileCircleCheck} />
					Save
				</button>
				<button
					type="button"
					class="button-neutral"
					on:click={() => (inEditor ? onRemove() : onDelete())}
				>
					<Fa icon={faTrash} />
					{inEditor ? 'Remove' : 'Delete'}
				</button>
			{/if}
		</div>
	</div>

	<div class="space-y-8 p-4 bg-surface-50-900-token pb-16 md:pb-4">
		<div class="space-y-4">
			<h4 class="h4">Properties</h4>

			<TextInput label="Name" name="name" bind:value={component.name} />

			<Popup>
				<svelte:fragment slot="trigger">
					<TextInput
						style="md:col-span-2"
						label="Target"
						name="target"
						bind:value={component.target}
						error={component.target === '' ? "Target can't be empty" : ''}
					/>
				</svelte:fragment>
				<svelte:fragment slot="popup">
					<div class="popup-solid flex items-center">
						<div class="p-4 px-8 flex items-center justify-center">
							<Fa icon={faInfo} size="2x" />
						</div>
						<p class="border-l border-color p-4 grow">
							The target can be a Docker image name (Docker, Swarm and Kubernetes Driver), a URL
							(Remote Driver) or a Java class path (UIMADriver).
						</p>
					</div>
				</svelte:fragment>
			</Popup>
			<Dropdown label="Driver" name="driver" options={DUUIDrivers} bind:value={component.driver} />

			<Chips style="md:col-span-2" label="Tags" bind:values={component.tags} />
			<TextArea
				style="md:col-span-2"
				label="Description"
				name="description"
				bind:value={component.description}
			/>
		</div>

		<div class="space-y-4 border-t border-color pt-4">
			<h4 class="h4">Options</h4>
			<ComponentOptions {component} />
		</div>

		<div class="space-y-4 border-t border-color pt-4">
			<h4 class="h4">Parameters</h4>
			<JsonInput bind:data={parameters} />
		</div>
	</div>
</div>
