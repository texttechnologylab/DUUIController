<script lang="ts">
	import { activePipelineStore } from '../../Interfaces/store';

	import Dialog from '../Dialog.svelte';
	import DriverIcon from '../DriverIcon.svelte';
	import DeleteButton from '../Buttons/DeleteButton.svelte';
	import ExpandButton from '../Buttons/ToggleButton.svelte';
	import { driverTypes, type DUUIPipelineComponent } from '../../Interfaces/interfaces';
	import TextInput from '../Forms/TextInput.svelte';
	import Dropdown from '../Forms/Dropdown.svelte';

	export let component: DUUIPipelineComponent;

	function deleteComponent() {
		$activePipelineStore = $activePipelineStore.filter(function (value, i, arr) {
			if (value.id !== component.id) return value;
		});
	}

	const handleDriverChange = () => {
		activePipelineStore.update((o) => {
			o.map((item) => {
				if (item.id === component.id) {
					component.driver = driverName;
				}
			});
			return o;
		});
	};

	let dialog: HTMLDialogElement;
	let expanded: boolean = false;
	let driverName: string = component.driver;

	let dockerImageName: string = '';
</script>

<li class="flex gap-4 items-start">
	<div class="flex flex-col grow border-[1px] border-slate-800 relative">
		<div class="flex gap-4">
			<div
				class="header flex text-slate-800 items-center grow {expanded
					? 'border-b-[1px]'
					: ''} border-slate-800"
			>
				<DriverIcon driver={component.driver} />

				<p
					class="text-md outline-none p-3 grow text-center border-x-[1px] border-x-slate-800"
				>
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
					<Dropdown
						label="Driver"
						options={driverTypes}
						bind:value={driverName}
						on:change={handleDriverChange}
					/>
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
				</div>
			</div>
		</div>
	</div>
	<DeleteButton on:click={() => dialog.showModal()} />
</li>

<Dialog bind:dialog on:accept={deleteComponent}>
	<p class="text-lg">Are you sure you want to delete {component.name}?</p>
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
