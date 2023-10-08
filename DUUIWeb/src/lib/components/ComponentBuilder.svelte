<script lang="ts">
	import { DUUIDrivers } from '$lib/data'
	import DriverIcon from './DriverIcon.svelte'
	import Fa from 'svelte-fa'
	import { faChevronDown } from '@fortawesome/free-solid-svg-icons'
	import { createEventDispatcher } from 'svelte'
	import { componentStore } from '../../routes/pipelines/new/store'
	import { ListBox, ListBoxItem, popup, type PopupSettings } from '@skeletonlabs/skeleton'
	import { driverTargetMap } from '../../routes/pipelines/new/toast'

	const dispatcher = createEventDispatcher()

	export let deleteButton: boolean = false

	let onRemove = () => {
		dispatcher('remove', {
			id: $componentStore.id
		})
	}

	const popupCombobox: PopupSettings = {
		event: 'focus-click',
		target: 'popupCombobox',
		placement: 'bottom-start',
		middleware: {
			offset: 8
		},
		closeQuery: '.listbox-item'
	}

	let useGPU: boolean = true
	let dockerImageFetching: boolean = true

	$: {
		$componentStore.settings.options['gpu'] = useGPU
		$componentStore.settings.options['dockerImageFetching'] = dockerImageFetching
	}
</script>

<div class="bg-surface-700 rounded-md shadow-lg z-100" data-popup="popupCombobox">
	<ListBox active="variant-filled-primary">
		{#each DUUIDrivers as driver}
			<ListBoxItem
				bind:group={$componentStore.settings.driver}
				name="medium"
				value={driver}
				class="text-sm md:text-base"
			>
				{driver}
			</ListBoxItem>
		{/each}
	</ListBox>
</div>

<div class="flex flex-col gap-4 p-4 pointer-events-auto text-sm md:text-base">
	<header class="flex justify-start gap-4 items-center">
		<DriverIcon driver={$componentStore.settings.driver} />
		<p class="md:h4">{$componentStore.name}</p>
		{#if deleteButton}
			<button class="btn variant-filled-error rounded-sm shadow-lg ml-auto" on:click={onRemove}>
				Delete
			</button>
		{/if}
	</header>
	<div class="grid md:grid-cols-2 gap-4">
		<label class="label">
			<span>Name*</span>
			<input
				class="input border-2 text-sm md:text-base"
				type="text"
				bind:value={$componentStore.name}
			/>
		</label>

		<label class="flex flex-col space-y-[0.125rem]">
			<span>Driver</span>
			<button
				class="flex grow justify-between items-center py-2 px-3 border-2 input"
				use:popup={popupCombobox}
			>
				<span class="capitalize text-sm md:text-base">{$componentStore.settings.driver}</span>
				<Fa icon={faChevronDown} />
			</button>
		</label>
		<label class="label md:col-span-2">
			<span>{driverTargetMap.get($componentStore.settings.driver)}*</span>
			<input
				class="input border-2 text-sm md:text-base"
				type="text"
				bind:value={$componentStore.settings.target}
			/>
		</label>
		<label class="label">
			<span>Category</span>
			<input
				class="input border-2 text-sm md:text-base"
				type="text"
				bind:value={$componentStore.category}
			/>
		</label>
		<label class="label md:col-span-2">
			<span>Description</span>
			<textarea
				class="textarea border-2 text-sm md:text-base"
				bind:value={$componentStore.description}
			/>
		</label>
	</div>
	<div class="space-y-1">
		<h3>Advanced Settings</h3>
		<div class="card p-4 variant-filled-secondary" data-popup="tooltip">
			<p>Hover Content</p>
			<div class="arrow variant-filled-secondary" />
		</div>
		<div class="grid grid-cols-1 card p-4 gap-4 pointer-events-auto">
			<label class="flex items-center space-x-2 text-sm md:text-base">
				<input class="checkbox" type="checkbox" bind:checked={useGPU} />
				<span>Utilize GPU</span>
			</label>
			<label class="flex items-start space-x-2 text-sm md:text-base">
				<input
					class="checkbox checked:variant-filled-primary"
					type="checkbox"
					bind:checked={dockerImageFetching}
				/>
				<span>Download Docker Image if unavailable</span>
			</label>
		</div>
	</div>
</div>
