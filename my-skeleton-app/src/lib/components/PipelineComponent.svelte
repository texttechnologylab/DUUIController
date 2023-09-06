<script lang="ts">
	import DriverIcon from './DriverIcon.svelte'
	import { DUUIDrivers, type DUUIPipelineComponent } from '$lib/data'
	import { createEventDispatcher } from 'svelte'
	import Fa from 'svelte-fa'
	import { faEdit } from '@fortawesome/free-solid-svg-icons'
	import { getModalStore, type ModalSettings } from '@skeletonlabs/skeleton'
	const dispatcher = createEventDispatcher()

	export let component: DUUIPipelineComponent

	const modalStore = getModalStore()

	export let editMode: boolean = false

	const onEditComponent = () => {
		editMode = !editMode
	}

	let onRemove = () => {
		dispatcher('remove', {
			id: component.id
		})
	}

	const onMaybeDelete = () => {
		new Promise<boolean>((resolve) => {
			const modal: ModalSettings = {
				type: 'confirm',
				title: 'Please Confirm',
				body: `Are you sure you wish to delete ${component.name}?`,
				response: (r: boolean) => {
					resolve(r)
				}
			}
			modalStore.trigger(modal)
		}).then((r: any) => {
			if (r) {
				onRemove()
			}
		})
	}
</script>

<li class="card flex flex-col gap-4 p-4 pointer-events-none">
	<div class="flex grid-cols-2 gap-4 items-center">
		<DriverIcon driver={component.driver} />
		<p class="h4 grow">{component.name}</p>
		<button
			class="btn-icon pointer-events-auto variant-glass-primary"
			on:click={() => onEditComponent()}
		>
			<span>
				<Fa size="md" icon={faEdit} />
			</span>
		</button>
	</div>
	<p class="hidden md:block self-stretch">{component.target}</p>
	{#if editMode}
		<form class="space-y-4 pointer-events-auto">
			<label class="label">
				<span>Name</span>
				<input class="input" type="text" placeholder="Name" bind:value={component.name} />
			</label>
			<label class="label">
				<span>Driver</span>
				<select class="select" bind:value={component.driver}>
					{#each DUUIDrivers as driver}
						<option value={driver}>{driver}</option>
					{/each}
				</select>
			</label>
			<label class="label">
				<span>Target</span>
				<input class="input" type="text" placeholder="Name" bind:value={component.target} />
			</label>

			<div class="flex justify-end">
				<button class="btn variant-filled-error rounded-sm shadow-lg" on:click={onMaybeDelete}>
					Delete
				</button>
			</div>
		</form>
	{/if}
</li>

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
