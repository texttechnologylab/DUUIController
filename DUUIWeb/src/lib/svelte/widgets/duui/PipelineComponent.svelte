<script lang="ts">
	import DriverIcon from '$lib/svelte/DriverIcon.svelte'
	import ActionButton from '../action/ActionButton.svelte'
	import Chips from '../input/Chips.svelte'
	import Dropdown from '../input/Dropdown.svelte'
	import TextArea from '../input/TextArea.svelte'
	import Text from '../input/TextInput.svelte'

	import pkg from 'lodash'
	const { cloneDeep, isEqual } = pkg

	import { DUUIDrivers, type DUUIComponent } from '$lib/duui/component'
	import { Api, makeApiCall } from '$lib/duui/utils/api'
	import { slugify } from '$lib/duui/utils/text'
	import { scrollIntoView, successToast, variantError, variantSuccess } from '$lib/duui/utils/ui'
	import {
		faFileCircleCheck,
		faFileCircleXmark,
		faFilePen,
		faTrash
	} from '@fortawesome/free-solid-svg-icons'
	import {
		Tab,
		TabGroup,
		getModalStore,
		getToastStore,
		type ModalSettings
	} from '@skeletonlabs/skeleton'
	import { createEventDispatcher } from 'svelte'
	import IconButton from '../action/IconButton.svelte'
	import JsonPreview from '../input/JsonInput.svelte'

	export let component: DUUIComponent

	const dispatcher = createEventDispatcher()

	export let expanded: boolean = false
	export let isNew: boolean = false

	const modalStore = getModalStore()
	const toastStore = getToastStore()

	let componentCopy: DUUIComponent = cloneDeep(component)

	let hasChanges: boolean = false
	let tabSet: number = 0

	let parameters: Map<string, string> = new Map(Object.entries(component.parameters))
	let options: Map<string, string> = new Map(Object.entries(component.options))

	$: hasChanges =
		!isEqual(component, componentCopy) ||
		!isEqual(
			{
				parameters: Object.fromEntries(parameters.entries()),
				options: Object.fromEntries(options.entries())
			},
			{ parameters: component.parameters, options: component.options }
		)

	const onUpdate = async () => {
		if (!isNew) {
			component.parameters = Object.fromEntries(parameters)
			component.options = Object.fromEntries(options)
			const response = await makeApiCall(Api.Components, 'PUT', component)

			if (response.ok) {
				toastStore.trigger(successToast('Component updated successfully'))
				componentCopy = cloneDeep(component)
				parameters = new Map(Object.entries(component.parameters))
				options = new Map(Object.entries(component.options))
			}

			dispatcher('updated', { ...component })
		}
	}

	const discardChanges = () => {
		component = cloneDeep(componentCopy)
		parameters = new Map(Object.entries(component.parameters))
		options = new Map(Object.entries(component.options))
	}

	const onRemove = () => {
		new Promise<boolean>((resolve) => {
			const modal: ModalSettings = {
				type: 'component',
				component: 'deleteModal',
				meta: {
					title: 'Remove Component',
					body: `Are you sure you want to remove ${component.name}?`
				},
				response: (r: boolean) => {
					resolve(r)
				}
			}

			modalStore.trigger(modal)
		}).then(async (accepted: boolean) => {
			if (!accepted) return
			dispatcher('remove', { id: component.id })
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
			toastStore.trigger(successToast('Component deleted successfully'))
			dispatcher('deleteComponent', { oid: component.oid })
		})
	}
</script>

<li
	id={slugify(component.name)}
	class="section-wrapper scroll-mt-4 md:scroll-mt-24
        {!expanded ? 'pointer-events-none ' : 'pointer-events-auto'}"
>
	<header
		class="flex justify-between gap-4 items-center px-4 py-2 bg-surface-50/100 dark:bg-surface-900/25"
	>
		<div class="md:flex md:items-center grid gap-4">
			<DriverIcon driver={component.driver} />
			<p class="{expanded ? 'hidden md:block' : ''} md:h4 grow">{component.name}</p>
		</div>
		<div
			class="scroll-mt-4 md:scroll-mt-16 flex-col-reverse gap-2 md:flex-row {expanded
				? 'space-x-1'
				: 'flex'}"
		>
			<IconButton
				_class="pointer-events-auto pl-1"
				variant="variant-soft-primary"
				rounded="rounded-full"
				icon={faFilePen}
				on:click={() => {
					expanded = !expanded
					if (!isNew) return

					if (expanded) {
						scrollIntoView(slugify(component.name))
					} else {
						scrollIntoView('top')
					}
				}}
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
					bind:value={component.driver}
				/>
				<Text
					style="md:col-span-2"
					label="Target"
					name="target"
					bind:value={component.target}
				/>

				<Chips style="md:col-span-2" label="Tags" bind:values={component.tags} />
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
						<JsonPreview bind:data={parameters} />
				
					{:else if tabSet === 1}
						<JsonPreview bind:data={options} />

			
					{/if}
				</div>
			</div>
			<hr class="bg-surface-400/20 h-[1px] !border-0 rounded" />

			{#if hasChanges}
				<footer class="bg-surface-50/100 dark:bg-surface-900/25">
					<div class="grid md:flex items-center md:justify-end px-4 py-2 gap-4">
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
