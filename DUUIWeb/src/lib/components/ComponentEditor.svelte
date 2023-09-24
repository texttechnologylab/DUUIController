<script lang="ts">
	import { DUUIDrivers, type DUUIPipelineComponent } from '$lib/data'
	import { getModalStore, type ModalSettings } from '@skeletonlabs/skeleton'
	import { createEventDispatcher } from 'svelte'
	const dispatcher = createEventDispatcher()

	const modalStore = getModalStore()

	export let component: DUUIPipelineComponent
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
				body: 'Are you sure you wish to delete this component?',
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

<form class="space-y-4 pointer-events-auto">
	<label class="label">
		<span>Name</span>
		<input class="input" type="text" placeholder="Name" bind:value={component.name} />
	</label>
	<label class="label">
		<span>Driver</span>
		<select class="select" bind:value={component.settings.driver}>
			{#each DUUIDrivers as driver}
				<option value={driver}>{driver}</option>
			{/each}
		</select>
	</label>
	<label class="label">
		<span>Target</span>
		<input class="input" type="text" placeholder="Name" bind:value={component.settings.target} />
	</label>

	<div class="grid grid-cols-2 gap-4">
		<button class="btn variant-ghost-success rounded-sm shadow-lg"> Save Changes </button>
		<button class="btn variant-ghost-error rounded-sm shadow-lg" on:click={onMaybeDelete}>
			Delete
		</button>
	</div>
</form>
