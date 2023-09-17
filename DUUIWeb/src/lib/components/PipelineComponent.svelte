<script lang="ts">
	import DriverIcon from './DriverIcon.svelte'
	import { DUUIDrivers, DUUIStatus, type DUUIPipelineComponent } from '$lib/data'
	import { createEventDispatcher } from 'svelte'
	import Fa from 'svelte-fa'
	import { faCheck, faEdit, faRefresh, faX } from '@fortawesome/free-solid-svg-icons'
	import { getModalStore, type ModalSettings } from '@skeletonlabs/skeleton'
	const dispatcher = createEventDispatcher()

	export let component: DUUIPipelineComponent

	const modalStore = getModalStore()

	export let editMode: boolean = false
	export let completed: boolean = false
	export let active: boolean = false

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

	function toggleAdvancedSettings(
		event: MouseEvent & { currentTarget: EventTarget & HTMLButtonElement }
	) {
		throw new Error('Function not implemented.')
	}
</script>

<li class="card shadow-lg flex flex-col gap-4 p-4 pointer-events-none">
	<div class="flex grid-cols-2 gap-4 items-center">
		{#if completed}
			<Fa icon={faCheck} size="lg" />
		{:else if active}
			<Fa icon={faRefresh} size="lg" class="animate-spin " />
		{/if}
		<DriverIcon driver={component.driver} />
		<p class="h4 grow">{component.name}</p>
		<button
			class="btn-icon pointer-events-auto variant-glass-primary"
			on:click={() => (editMode = !editMode)}
		>
			<span>
				<Fa size="md" icon={faEdit} />
			</span>
		</button>
	</div>
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
				{#if component.driver === 'DUUIRemoteDriver'}
					<span>Target</span>
				{:else if component.driver === 'DUUIUIMADriver'}
					<span>Class Path</span>
				{:else}
					<span>Image Name</span>
				{/if}
				<input class="input" type="text" placeholder="Name" bind:value={component.target} />
			</label>

			<div class="flex justify-between gap-4">
				<button
					class="btn variant-filled-primary rounded-sm shadow-lg"
					on:click={toggleAdvancedSettings}
				>
					Advanced Settings
				</button>
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
