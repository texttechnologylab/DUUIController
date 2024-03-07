<!--
 @component 
 This component is used in different places as a way to add DUUIComponents to a pipeline.
 It consists of a button (trigger for the popup) and a container for multiple actions (buttons)
 that appears when hovering the trigger.

 The popup position can be set top or bottom.
-->
<script lang="ts">
	import { componentDrawerSettings } from '$lib/config'
	import { blankComponent, type DUUIComponent } from '$lib/duui/component'
	import { currentPipelineStore } from '$lib/store'
	import { faFileImport, faPlus, faToolbox } from '@fortawesome/free-solid-svg-icons'
	import { FileButton, getDrawerStore, getModalStore } from '@skeletonlabs/skeleton'
	import Fa from 'svelte-fa'
	import { v4 as uuidv4 } from 'uuid'
	import Popup from './Popup.svelte'

	export let templateComponents: DUUIComponent[] = []
	export let position: 'top' | 'bottom' = 'top'
	export let inEditor: boolean = false
	export let index: number = $currentPipelineStore.components.length

	const modalStore = getModalStore()
	const drawerStore = getDrawerStore()

	let files: FileList

	const importComponent = async () => {
		if (!files) return
		const file: File = files[0]
		async function parseJsonFile(file) {
			return new Promise((resolve, reject) => {
				const fileReader = new FileReader()
				fileReader.onload = (event) => resolve(JSON.parse(event.target.result))
				fileReader.onerror = (error) => reject(error)
				fileReader.readAsText(file)
			})
		}
		const result = await parseJsonFile(file)
		if (!result) return

		let component = blankComponent($currentPipelineStore.oid, index)

		drawerStore.open({
			id: 'component',
			...componentDrawerSettings,
			meta: {
				component: { ...component, ...result, id: uuidv4() },
				inEditor: inEditor,
				creating: true
			}
		})
	}

	const addComponent = () => {
		drawerStore.open({
			id: 'component',
			...componentDrawerSettings,
			meta: {
				component: blankComponent($currentPipelineStore.oid, index),
				inEditor: inEditor,
				creating: true
			}
		})
	}
</script>

<Popup {position}>
	<svelte:fragment slot="trigger">
		<button class="button-neutral bg-surface-100-800-token !aspect-square !rounded-full !p-3">
			<Fa icon={faPlus} />
		</button>
	</svelte:fragment>
	<svelte:fragment slot="popup">
		<div class="popup-solid">
			<div class="grid p-4 gap-2">
				<button class="button-neutral !border-none !justify-start" on:click={addComponent}>
					<Fa icon={faPlus} />
					<span>New</span>
				</button>
				{#if templateComponents.length > 0}
					<button
						class="button-neutral !border-none !justify-start"
						on:click={() => {
							modalStore.trigger({
								type: 'component',
								component: 'templateModal',
								meta: { templates: templateComponents }
							})
						}}
					>
						<Fa icon={faToolbox} />
						<span>Template</span>
					</button>
				{/if}

				<FileButton
					bind:files
					name="files"
					button="button-neutral !border-none !justify-start w-full"
					accept=".json"
					on:change={importComponent}
				>
					<Fa icon={faFileImport} />
					<span>Import</span>
				</FileButton>
			</div>
		</div>
	</svelte:fragment>
</Popup>
