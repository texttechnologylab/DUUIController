<script lang="ts">
	import ActionButton from '../svelte/widgets/action/ActionButton.svelte'
	import { DUUIDrivers, type DUUIComponent } from '$lib/duui/component'
	import { getModalStore, getToastStore, type ModalSettings } from '@skeletonlabs/skeleton'
	import { isEqual } from 'lodash'

	import IconButton from '../svelte/widgets/action/IconButton.svelte'
	import { faClose, faFilePen, faTrash } from '@fortawesome/free-solid-svg-icons'
	import { toTitleCase } from '$lib/utils/text'
	import { createEventDispatcher } from 'svelte'
	import { currentPipelineStore, markedForDeletionStore } from '$lib/store'
	import { success } from '$lib/utils/ui'
	import { makeApiCall, Api } from '$lib/utils/api'
	import TextInput from '$lib/svelte/widgets/input/TextInput.svelte'
	import ComboBox from '$lib/svelte/widgets/input/ComboBox.svelte'
	import Fa from 'svelte-fa'
	import TextArea from '$lib/svelte/widgets/input/TextArea.svelte'
	import SettingsMapper from '$lib/svelte/widgets/input/SettingsMapper.svelte'
	import DriverIcon from './DriverIcon.svelte'

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

	let categories: string[] = [...component.categories, 'A', 'B']
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
		<header class="flex flex-col">
			<div class="flex justify-between items-center shadow-lg p-4">
				<DriverIcon {driver} />
				<h3 class="h3">{name}</h3>
				<IconButton icon={faClose} on:click={() => modalStore.close()} rounded="rounded-full" />
			</div>
		</header>

		<div class="grid md:grid-cols-2 gap-4 p-4">
			<div class="space-y-4">
				<div class="space-y-4">
					<TextInput name="name" placeholder="Name" bind:value={name} />
					<ComboBox id="driver" name="driver" options={DUUIDrivers} bind:value={driver} />
					<TextInput name="target" placeholder="Target" bind:value={target} />
				</div>

				<div class="space-y-1">
					<span class="uppercase text-xs tracking-widest">Categories</span>
					<div
						class="flex flex-col focus:ring-0 focus-within:border-primary-500 border-[1px] border-surface-500"
					>
						<input
							class="{categories.length > 0
								? ' ring-0'
								: ''} border-none appearance-none ring-0 bg-transparent focus:ring-0 outline-none"
							type="text"
							bind:value={chipText}
							placeholder="Category"
							on:keypress={(event) => {
								if (event.key === 'Enter') {
									addCategory()
								}
							}}
						/>
						<div class={categories.length === 0 ? 'invisible' : 'flex flex-wrap gap-2 p-2'}>
							{#each categories as category}
								<!-- svelte-ignore a11y-no-static-element-interactions -->
								<button class="chip variant-glass-primary" on:click={() => removeCategory(category)}
									><span>
										{category}
									</span>
									<Fa icon={faClose} size="xs" />
								</button>
							{/each}
						</div>
					</div>
				</div>
			</div>

			<div class="space-y-4">
				<SettingsMapper />
			</div>
			<TextArea name="Description" rows={4} classes="md:col-span-2" />
		</div>
		<hr />

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
