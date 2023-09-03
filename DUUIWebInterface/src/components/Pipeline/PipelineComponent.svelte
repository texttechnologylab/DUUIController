<script lang="ts">
	const dispatcher = createEventDispatcher();

	import Dialog from '../Dialog.svelte';
	import DriverIcon from '../DriverIcon.svelte';
	import DeleteButton from '../Buttons/DeleteButton.svelte';
	import ExpandButton from '../Buttons/ToggleButton.svelte';
	import TextInput from '../Forms/TextInput.svelte';
	import Dropdown from '../Forms/Dropdown.svelte';
	import { createEventDispatcher } from 'svelte';
	import { DUUIDrivers, type DUUIPipelineComponent } from '$lib/data';

	export let component: DUUIPipelineComponent;

	let deletion = () => {
		dispatcher('deletion', {
			id: component.id
		});
		dialog.close();
	};

	let dialog: HTMLDialogElement;
	let expanded: boolean = false;
</script>

<li class="flex gap-4 items-start bg-white">
	<div class="flex flex-col grow border-[1px] border-slate-800 relative">
		<div class="flex gap-4">
			<div
				class="header flex text-slate-800 items-center grow {expanded
					? 'border-b-[1px]'
					: ''} border-slate-800"
			>
				<DriverIcon driver={component.driver} />

				<p class="text-md outline-none p-3 grow text-center border-x-[1px] border-x-slate-800">
					{component.name}
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
					<TextInput label="Name" bind:value={component.name} />
					<Dropdown label="Driver" options={DUUIDrivers} bind:value={component.driver} />
					<div class="col-span-2">
						{#if component.driver === 'DUUIRemoteDriver'}
							<TextInput label="Address" bind:value={component.target} />
						{:else if component.driver === 'DUUIDockerDriver'}
							<TextInput label="Image name" bind:value={component.target} />
						{:else if component.driver === 'DUUISwarmDriver'}
							<TextInput label="Image name" bind:value={component.target} />
						{:else}
							<TextInput label="Class Path" bind:value={component.target} />
						{/if}
					</div>
					<div class="col-span-2">
						<button
							class="bg-red-600 text-white font-body pointer px-4 py-2 rounded-md shadow-md"
							on:click={() => dialog.showModal()}>Delete</button
						>
					</div>
				</div>
			</div>
		</div>
	</div>
	<!-- <DeleteButton on:click={() => dialog.showModal()} /> -->
</li>

<Dialog bind:dialog on:accept={deletion}>
	<p class="text-lg">Delete Component</p>
	<p class="text-md max-w-[40ch]">
		Are you sure you want to delete <strong>{component.name}</strong>? This cannot be undone.
	</p>
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
