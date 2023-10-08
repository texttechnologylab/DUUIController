<script lang="ts">
	import { DUUIDockerDriver, DUUIDrivers, DUUIRemoteDriver, DUUISwarmDriver } from '$lib/data'
	import DriverIcon from './DriverIcon.svelte'
	import Fa from 'svelte-fa'
	import { faChevronDown } from '@fortawesome/free-solid-svg-icons'
	import { createEventDispatcher } from 'svelte'
	import { componentStore } from '../../routes/pipelines/new/store'
	import { ListBox, ListBoxItem, popup, type PopupSettings } from '@skeletonlabs/skeleton'
	import { driverTargetMap } from '../../routes/pipelines/new/toast'
	import DockerSettings from './Settings/DockerSettings.svelte'
	import RemoteSettings from './Settings/RemoteSettings.svelte'

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
</script>

<div class="bg-surface-700 shadow-lg z-100" data-popup="popupCombobox">
	<ListBox active="variant-filled-primary">
		{#each DUUIDrivers as driver}
			<ListBoxItem bind:group={$componentStore.settings.driver} name="medium" value={driver}>
				{driver}
			</ListBoxItem>
		{/each}
	</ListBox>
</div>

<div class="cflex flex-col gap-4 p-4 pointer-events-auto">
	<header class="flex justify-start gap-4 items-center">
		<DriverIcon driver={$componentStore.settings.driver} />
		<p class="h4">{$componentStore.name}</p>
		{#if deleteButton}
			<button class="btn variant-filled-error rounded-sm shadow-lg ml-auto" on:click={onRemove}>
				Delete
			</button>
		{/if}
	</header>
	<div class="grid grid-cols-2 gap-4">
		<label class="label">
			<span>Name*</span>
			<input
				class="input border-2"
				type="text"
				placeholder="Name"
				bind:value={$componentStore.name}
			/>
		</label>
		<label class="flex flex-col space-y-1">
			<span>Driver</span>
			<button
				class="flex grow bg-surface-700 justify-between items-center px-4 border-2 input"
				use:popup={popupCombobox}
			>
				<span class="capitalize">{$componentStore.settings.driver}</span>
				<Fa icon={faChevronDown} />
			</button>
		</label>
		<label class="label col-span-2">
			<span>{driverTargetMap.get($componentStore.settings.driver)}*</span>
			<input
				class="input border-2"
				type="text"
				placeholder="Name"
				bind:value={$componentStore.settings.target}
			/>
		</label>
		<label class="label">
			<span>Category</span>
			<input
				class="input border-2"
				type="text"
				placeholder="Category"
				bind:value={$componentStore.category}
			/>
		</label>
		<label class="label col-span-2">
			<span>Description</span>
			<textarea
				class="textarea border-2"
				placeholder="My new Component for..."
				bind:value={$componentStore.description}
			/>
		</label>
	</div>
	<h3>Advanced Settings</h3>
	{#if $componentStore.settings.driver === DUUIDockerDriver || $componentStore.settings.driver === DUUISwarmDriver}
		<DockerSettings />
	{:else if $componentStore.settings.driver === DUUIRemoteDriver}
		<RemoteSettings  />
	{/if}
	<!-- 
	{#if expanded}
		
	{/if} -->
	<!-- <button
		class="btn-icon variant-soft-surface self-center pointer-events-auto"
		on:click={() => (expanded = !expanded)}
	>
		<Fa icon={expanded ? faCaretUp : faCaretDown} />
	</button> -->
</div>
