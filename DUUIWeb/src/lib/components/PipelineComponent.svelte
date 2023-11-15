<script lang="ts">
	import DriverIcon from './DriverIcon.svelte'
	import { createEventDispatcher } from 'svelte'

	import { faFilePen } from '@fortawesome/free-solid-svg-icons'
	import { getModalStore, type ModalComponent, type ModalSettings } from '@skeletonlabs/skeleton'
	import type { DUUIComponent } from '$lib/duui/component'
	import IconButton from '../svelte/widgets/action/IconButton.svelte'
	import ComponentModal from '../svelte/widgets/modal/Component.svelte'
	export let component: DUUIComponent

	const modalStore = getModalStore()

	const modalComponent: ModalComponent = {
		ref: ComponentModal
	}

	const showComponentModal = () => {
		const modal: ModalSettings = {
			type: 'component',
			component: modalComponent,
			meta: { component: component }
		}
		modalStore.trigger(modal)
	}
</script>

<li
	class="dark:variant-filled-surface variant-glass shadow-lg flex flex-col gap-4 px-4 py-2 pointer-events-none"
>
	<div class="flex justify-between gap-4 md:gap-32 items-center">
		<div class="md:flex md:items-center grid gap-4">
			<DriverIcon driver={component.settings.driver} />
			<p class="md:h4 grow text-sm">{component.name}</p>
		</div>
		<IconButton
			_class="pointer-events-auto pl-1"
			variant="variant-soft-primary"
			rounded="rounded-full"
			icon={faFilePen}
			on:click={showComponentModal}
		/>
	</div>
	<!-- {#if editMode}
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

			<div class="flex gap-4 justify-between">
				<button
					class="btn variant-soft-surface shadow-lg {hasChanges ? 'inline-flex' : 'invisible'}"
					on:click={updateComponent}
				>
					<Fa icon={faFilePen} />
					<span>Save Changes</span>
				</button>

				<ActionButton
					text="Delete"
					icon={faTrash}
					variant="variant-soft-error"
					on:click={onDelete}
				/>
			</div>
		</form>
	{/if} -->
</li>
