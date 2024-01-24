<script lang="ts">
	import { DUUIDrivers, type DUUIComponent } from '$lib/duui/component'
	import { Tab, TabGroup, getModalStore } from '@skeletonlabs/skeleton'
	import ActionButton from '../action/ActionButton.svelte'

	import Chips from '$lib/svelte/widgets/input/Chips.svelte'
	import Dropdown from '$lib/svelte/widgets/input/Dropdown.svelte'
	import TextArea from '$lib/svelte/widgets/input/TextArea.svelte'
	import Text from '$lib/svelte/widgets/input/TextInput.svelte'
	import { faClose, faFilePen, faFileUpload } from '@fortawesome/free-solid-svg-icons'
	import DriverIcon from '../../DriverIcon.svelte'

	import { userSession } from '$lib/store'
	import Fa from 'svelte-fa'
	import JsonPreview from '../input/JsonInput.svelte'

	const modalStore = getModalStore()

	let component: DUUIComponent = $modalStore[0].meta.component

	let tabSet: number = 0

	let parameters: Map<string, string> = new Map(Object.entries(component.parameters))
	let options: Map<string, string> = new Map(Object.entries(component.options))

	const createComponent = async () => {
		component.options = Object.fromEntries(options.entries())
		component.parameters = Object.fromEntries(parameters.entries())

		if ($modalStore[0].response) {
			$modalStore[0].response({ accepted: true, component: component })
		}
		modalStore.close()
	}

	const uploadComponent = async () => {
		component.options = Object.fromEntries(options.entries())
		component.parameters = Object.fromEntries(parameters.entries())

		const response = await fetch('/api/components', {
			method: 'POST',
			body: JSON.stringify({
				component: component
			})
		})

		if (response.ok) {
			modalStore.close()
		}
	}
</script>

<div class="card rounded-none shadow-lg container max-w-5xl max-h-[90vh]">
	<div
		class="flex justify-between items-center bg-surface-200/20 dark:bg-surface-900/25 p-4 py-3 sticky top-0"
	>
		<DriverIcon driver={component.driver} />
		<button
			class="transition-colors text-surface-700 hover:text-error-500"
			on:click={() => modalStore.close()}
		>
			<Fa icon={faClose} size="lg" />
		</button>
	</div>
	<hr class="bg-surface-400/20 h-[1px] !border-0 rounded" />

	<div class="grid md:grid-cols-2 gap-4 p-4 max-h-[20rem] md:max-h-[40rem] overflow-y-scroll">
		<Text label="Name" name="name" bind:value={component.name} />
		<Dropdown label="Driver" name="driver" options={DUUIDrivers} bind:value={component.driver} />
		<Text style="md:col-span-2" label="Target" name="target" bind:value={component.target} />
		<!-- <SettingsMapper /> -->

		<Chips style="md:col-span-2" label="Categories" bind:values={component.tags} />
		<TextArea
			style="md:col-span-2"
			label="Description"
			name="description"
			bind:value={component.description}
		/>
		<TabGroup rounded="rounded-none" active="border-b-2 border-b-primary-500" border="border-none">
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

	<footer class="bg-surface-200/20 dark:bg-surface-900/25 p-4">
		<div class="grid grid-cols-2 gap-4">
			{#if $userSession?.role === 'Admin'}
				<ActionButton
					disabled={!component.name || !component.driver || !component.target}
					text="Upload"
					icon={faFileUpload}
					on:click={uploadComponent}
				/>
			{/if}
			<ActionButton
				disabled={!component.name || !component.driver || !component.target}
				text="Create"
				icon={faFilePen}
				on:click={createComponent}
			/>
		</div>
	</footer>
</div>
