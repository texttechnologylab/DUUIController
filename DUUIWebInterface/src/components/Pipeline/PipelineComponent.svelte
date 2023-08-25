<script lang="ts">
	import { createEventDispatcher } from 'svelte';
	let dispatch = createEventDispatcher();

	import Dialog from '../Dialog.svelte';
	import DriverIcon from '../DriverIcon.svelte';
	import DeleteButton from '../Buttons/DeleteButton.svelte';
	import ExpandButton from '../Buttons/ToggleButton.svelte';
	import { driverTypes, type DUUIPipelineComponent } from '../../Interfaces/interfaces';
	import TextInput from '../Forms/TextInput.svelte';
	import Dropdown from '../Forms/Dropdown.svelte';
	import { Icon } from 'flowbite-svelte-icons';

	export let component: DUUIPipelineComponent;

	function deleteComponent() {
		dispatch('delete', {
			item: component
		});
	}
	let dialog: HTMLDialogElement;

	export let expanded: boolean = true;
</script>

<li draggable={true} on:dragstart on:dragenter class="flex gap-4 items-start">
	<div class="py-3 flex items-center justify-center cursor-grab text-slate-400 hover:text-slate-800">
		<Icon name="dots-vertical-outline" class="w-6 h-6" />
	</div>
	<div class="flex flex-col grow border-[1px] border-slate-800 relative">
		<div class="flex gap-4">
			<div
				class="header flex text-slate-800 items-center grow {expanded
					? 'border-b-[1px]'
					: ''} border-slate-800"
			>
				<DriverIcon driver={component.driver} />

				<p class="text-md cursor-pointer outline-none border-r-[1px] p-3 border-slate-800 grow">
					{component.displayName}
				</p>

				<ExpandButton
					on:collapse={() => (expanded = !expanded)}
					on:expand={() => (expanded = !expanded)}
				/>
			</div>
		</div>

		<div class="grid-container" aria-expanded={expanded ? 'true' : 'false'}>
			<div class="overflow-hidden">
				<div class="p-4 grid grid-cols-2 gap-4 justify-center overflow-hidden">
					<TextInput label="Name" bind:value={component.displayName} />
					<Dropdown label="Driver" options={driverTypes} bind:value={component.driver} />
					<slot />
				</div>
			</div>
		</div>
	</div>
	<DeleteButton on:click={() => dialog.showModal()} />
</li>

<Dialog bind:dialog on:accept={deleteComponent}>
	<p class="text-lg">Are you sure you want to delete {component.displayName}?</p>
</Dialog>

<style>
	.grid-container {
		display: grid;
		grid-template-rows: 0fr;

		transition: grid-template-rows 500ms ease-out;
	}

	.grid-container[aria-expanded='true'] {
		grid-template-rows: 1fr;
	}
</style>
