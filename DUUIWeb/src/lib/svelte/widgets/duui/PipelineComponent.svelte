<script lang="ts">
	import DriverIcon from '$lib/components/DriverIcon.svelte'
	import Text from '../input/TextInput.svelte'
	import TextArea from '../input/TextArea.svelte'
	import Chips from '../input/Chips.svelte'
	import Dropdown from '../input/Dropdown.svelte'
	import ActionButton from '../action/ActionButton.svelte'

	import { isEqual, cloneDeep } from 'lodash'

	import {
		faFileCircleCheck,
		faFileCircleXmark,
		faFilePen,
		faTrash
	} from '@fortawesome/free-solid-svg-icons'
	import { DUUIDrivers, type DUUIComponent } from '$lib/duui/component'
	import IconButton from '../action/IconButton.svelte'
	import Mapper from '../input/Mapper.svelte'
	import { makeApiCall, Api } from '$lib/utils/api'
	import { success, variantError, variantSuccess } from '$lib/utils/ui'
	import {
		getModalStore,
		getToastStore,
		TabGroup,
		type ModalSettings,
		Tab
	} from '@skeletonlabs/skeleton'
	import { createEventDispatcher } from 'svelte'

	export let component: DUUIComponent

	const dispatcher = createEventDispatcher()

	export let expanded: boolean = false
	export let isNew: boolean = false

	const modalStore = getModalStore()
	const toastStore = getToastStore()

	let componentCopy: DUUIComponent = cloneDeep(component)

	let hasChanges: boolean = false
	let tabSet: number = 0

	let parameters: Map<string, string> = new Map(Object.entries(component.settings.parameters))
	let options: Map<string, string> = new Map(Object.entries(component.settings.options))

	$: hasChanges = !isEqual(component, componentCopy)

	const onUpdate = async () => {
		if (!isNew) {
			const response = await makeApiCall(Api.Components, 'PUT', component)
			if (response.ok) {
				toastStore.trigger(success('Component updated successfully'))
				componentCopy = cloneDeep(component)
			}
		}
	}

	const discardChanges = () => {
		component = cloneDeep(componentCopy)
		parameters = new Map(Object.entries(component.settings.parameters))
		options = new Map(Object.entries(component.settings.options))
	}

	const onRemove = () => {
		new Promise<boolean>((resolve) => {
			const modal: ModalSettings = {
				type: 'component',
				component: 'deleteModal',
				meta: {
					title: 'Remove Component',
					body: `Are you sure you want to delete ${component.name}?`
				},
				response: (r: boolean) => {
					resolve(r)
				}
			}

			modalStore.trigger(modal)
		}).then(async (accepted: boolean) => {
			if (!accepted) return
			dispatcher('remove', { oid: component.oid })
		})
	}

	const onDelete = async () => {
		new Promise<boolean>((resolve) => {
			const modal: ModalSettings = {
				type: 'component',
				component: 'deleteModal',
				meta: {
					title: 'Delete Component',
					body: `Are you sure you want to delete ${component.name}?`
				},
				response: (r: boolean) => {
					resolve(r)
				}
			}

			modalStore.trigger(modal)
		}).then(async (accepted: boolean) => {
			if (!accepted) return

			makeApiCall(Api.Components, 'DELETE', component)
			toastStore.trigger(success('Component deleted successfully'))
			dispatcher('deleteComponent', { oid: component.oid })
		})
	}
</script>

<li
	class="rounded-md border border-surface-200 dark:border-surface-500 shadow-lg overflow-hidden
        {!expanded ? 'pointer-events-none ' : 'pointer-events-auto'}"
>
	<header
		class="flex justify-between gap-4 items-center px-4 py-2 bg-surface-50/100 dark:bg-surface-900/25"
	>
		<div class="md:flex md:items-center grid gap-4">
			<DriverIcon driver={component.settings.driver} />
			{#if !expanded}
				<p class="md:h4 grow">{component.name}</p>
			{/if}
		</div>
		<div class=" flex-col-reverse gap-2 md:flex-row {expanded ? 'space-x-1' : 'flex'}">
			<IconButton
				_class="pointer-events-auto pl-1"
				variant="variant-soft-primary"
				rounded="rounded-full"
				icon={faFilePen}
				on:click={() => (expanded = !expanded)}
			/>
			<IconButton
				_class="pointer-events-auto"
				icon={faTrash}
				variant={variantError}
				on:click={() => (isNew ? onRemove() : onDelete())}
			/>
		</div>
	</header>

	{#if expanded}
		<div class="rounded-md items-start justify-start pointer-events-auto">
			<hr class="bg-surface-400/20 h-[1px] !border-0 rounded" />

			<div class="grid md:grid-cols-2 gap-4 p-4">
				<Text label="Name" name="name" bind:value={component.name} />
				<Dropdown
					label="Driver"
					name="driver"
					options={DUUIDrivers}
					bind:value={component.settings.driver}
				/>
				<Text
					style="md:col-span-2"
					label="Target"
					name="target"
					bind:value={component.settings.target}
				/>
				<!-- <SettingsMapper /> -->

				<Chips
					style="md:col-span-2"
					label="Categories"
					bind:values={component.categories}
					on:push={() => console.log(component.categories)}
				/>
				<TextArea
					style="md:col-span-2"
					label="Description"
					name="description"
					bind:value={component.description}
				/>
				<TabGroup
					rounded="rounded-none"
					active="border-b-2 border-b-primary-500"
					border="border-none"
				>
					<Tab name="Parameters" value={0} bind:group={tabSet}>Parameters</Tab>
					<Tab name="Options" value={1} bind:group={tabSet}>Options</Tab>
				</TabGroup>
				<div class="md:col-span-2">
					{#if tabSet === 0}
						<Mapper
							label="Parameters"
							bind:map={parameters}
							on:update={(event) => {
								component.settings.parameters = Object.fromEntries(event.detail.map.entries())
							}}
						/>
					{:else if tabSet === 1}
						<Mapper
							label="Options"
							bind:map={options}
							on:update={(event) => {
								component.settings.options = Object.fromEntries(event.detail.map.entries())
							}}
						/>
					{/if}
				</div>
			</div>
			<hr class="bg-surface-400/20 h-[1px] !border-0 rounded" />

			{#if hasChanges}
				<footer class="bg-surface-50/100 dark:bg-surface-900/25">
					<div class="flex items-center justify-end px-4 py-2 gap-4">
						<ActionButton
							visible={hasChanges && !isNew}
							variant={variantSuccess}
							text="Save Changes"
							icon={faFileCircleCheck}
							on:click={onUpdate}
						/>
						<ActionButton
							visible={hasChanges && !isNew}
							variant={variantError}
							text="Discard Changes"
							icon={faFileCircleXmark}
							on:click={discardChanges}
						/>
					</div>
				</footer>
			{/if}
		</div>
	{/if}
</li>
