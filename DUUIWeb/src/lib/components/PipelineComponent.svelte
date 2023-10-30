<script lang="ts">
	import DriverIcon from './DriverIcon.svelte'
	import { createEventDispatcher } from 'svelte'
	import Fa from 'svelte-fa'
	import { faEdit } from '@fortawesome/free-solid-svg-icons'
	import { getModalStore, getToastStore, type ModalSettings } from '@skeletonlabs/skeleton'
	import { DUUIDrivers, type DUUIComponent } from '$lib/duui/component'
	import { Api, makeApiCall } from '$lib/utils/api'
	import { success } from '$lib/utils/ui'
	const dispatcher = createEventDispatcher()

	export let component: DUUIComponent

	const modalStore = getModalStore()
	const toastStore = getToastStore()

	export let editMode: boolean = false

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

			let response = await makeApiCall(Api.Pipelines, 'DELETE', {
				id: component.id,
				component: true
			})
			
			if (response.ok) {
				toastStore.trigger(success('Component deleted successfully'))
				dispatcher('delete', { component: component })
			}
		})
	}

	const onUpdate = () => {
		dispatcher('update', {})
		editMode = false
	}
</script>

<li class="card rounded-md shadow-lg flex flex-col gap-4 px-4 py-2 pointer-events-none">
	<div class="flex justify-between gap-2 items-center">
		<div class="grid gap-2">
			<DriverIcon driver={component.settings.driver} />
			<p class="md:h4 grow text-sm">{component.name}</p>
		</div>
		<button
			class="btn-icon pointer-events-auto variant-ringed-surface"
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
				<input class="border-2 input" type="text" placeholder="Name" bind:value={component.name} />
			</label>
			<label class="label">
				<span>Driver</span>
				<select class="border-2 select input" bind:value={component.settings.driver}>
					{#each DUUIDrivers as driver}
						<option value={driver}>{driver}</option>
					{/each}
				</select>
			</label>
			<label class="label">
				{#if component.settings.driver === 'DUUIRemoteDriver'}
					<span>Target</span>
				{:else if component.settings.driver === 'DUUIUIMADriver'}
					<span>Class Path</span>
				{:else}
					<span>Image Name</span>
				{/if}
				<input
					class="border-2 input"
					type="text"
					placeholder="Name"
					bind:value={component.settings.target}
				/>
			</label>

			<div class="flex justify-between gap-4">
				<button class="btn variant-filled-primary shadow-lg" on:click={onUpdate}> Update </button>
				<button class="btn variant-filled-error shadow-lg" on:click={onDelete}> Delete </button>
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
