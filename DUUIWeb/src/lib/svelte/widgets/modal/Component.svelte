<script lang="ts">
	import ActionButton from '../action/ActionButton.svelte'
	import { DUUIDrivers, type DUUIComponent } from '$lib/duui/component'
	import { getModalStore, getToastStore, type ModalSettings } from '@skeletonlabs/skeleton'
	import { isEqual } from 'lodash'

	import IconButton from '../action/IconButton.svelte'
	import { faClose, faFilePen, faTrash } from '@fortawesome/free-solid-svg-icons'
	import { toTitleCase } from '$lib/utils/text'
	import { createEventDispatcher } from 'svelte'
	import { currentPipelineStore, markedForDeletionStore } from '$lib/store'
	import { success } from '$lib/utils/ui'
	import { makeApiCall, Api } from '$lib/utils/api'
	import TextInput from '$lib/svelte/widgets/input/TextInput.svelte'
	import ComboBox from '$lib/svelte/widgets/input/Select.svelte'
	import Fa from 'svelte-fa'
	import TextArea from '$lib/svelte/widgets/input/TextArea.svelte'
	import SettingsMapper from '$lib/svelte/widgets/input/SettingsMapper.svelte'
	import DriverIcon from '../../../components/DriverIcon.svelte'
	import Text from '$lib/svelte/widgets/input/Text.svelte'
	import Select from '$lib/svelte/widgets/input/Select.svelte'
	import Dropdown from '$lib/svelte/widgets/input/Dropdown.svelte'
	import Chips from '$lib/svelte/widgets/input/Chips.svelte'

	const dispatcher = createEventDispatcher()
	const modalStore = getModalStore()

	let component: DUUIComponent = $modalStore[0].meta.component
	const newComponent: boolean = $modalStore[0].meta.new || false

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

		$markedForDeletionStore.push(component.oid)
		modalStore.close()
	}

	const addCategory = () => {
		if (chipText.length > 0 && !categories.includes(chipText)) {
			categories = [...categories, toTitleCase(chipText)]
		}
		chipText = ''
	}

	const removeCategory = (category: string) => {
		console.log(category)

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

			toastStore.trigger(success('Component deleted successfully'))
			dispatcher('delete', { component: component })
		})
	}

	const updateComponent = async () => {
		if (newComponent) {
			if ($modalStore[0].response) {
				$modalStore[0].response({ accepted: true, component: component })
			}
			modalStore.close()
			return
		}
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
	<div class="card rounded-none items-start justify-start shadow-lg container max-w-5xl">
		<div class="flex justify-between items-center p-4">
			<DriverIcon {driver} />
			<h3 class="h3">{name}</h3>
			<IconButton icon={faClose} on:click={() => modalStore.close()} rounded="rounded-full" variant="variant-glass" />
		</div>
		<hr class="bg-surface-400/20 h-[1px] !border-0 rounded " />

		<div class="grid md:grid-cols-2 gap-4 p-4">
			<div class="space-y-4">
				<Text label="Name" name="name" bind:value={name} />
				<Dropdown label="Driver" name="driver" options={DUUIDrivers} bind:value={driver} />
				<Text label="Target" name="target" bind:value={target} />
			</div>
			<SettingsMapper />
			<Chips label="Categories" bind:values={component.categories} style="md:col-span-2"  />
			<TextArea label="Description" name="description" style="md:col-span-2" />
		</div>
		<hr class="bg-surface-400/20 h-[1px] !border-0 rounded " />

		<footer class="flex flex-col">
			<div class="flex justify-between items-center shadow-lg p-4">
				<ActionButton
					text={newComponent ? 'Create' : 'Update'}
					icon={faFilePen}
					on:click={updateComponent}
				/>
				<ActionButton
					text={newComponent ? 'Cancel' : 'Delete'}
					icon={faTrash}
					variant="dark:variant-soft-error variant-filled-error"
					on:click={() => (newComponent ? modalStore.close() : deleteComponent())}
				/>
			</div>
		</footer>
	</div>
{/if}
