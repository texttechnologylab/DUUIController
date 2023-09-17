<script lang="ts">
	import {
		DUUIDockerDriver,
		DUUIDrivers,
		DUUISwarmDriver,
		type DUUIPipelineComponent,
		DUUIRemoteDriver
	} from '$lib/data'
	import DriverIcon from './DriverIcon.svelte'
	import Fa from 'svelte-fa'
	import { faCaretDown, faCaretUp } from '@fortawesome/free-solid-svg-icons'
	import DockerSettings from './Settings/DockerSettings.svelte'
	import RemoteSettings from './Settings/RemoteSettings.svelte'
	import { error } from '@sveltejs/kit'
	import Dropdown from './Dropdown.svelte'

	export let component: DUUIPipelineComponent
	export let exception: string

	let expanded: boolean = false
</script>

<div class="card shadow-lg flex flex-col gap-4 p-4 pointer-events-auto">
	<header class="flex justify-start gap-4 items-center">
		<DriverIcon driver={component.driver} />
		<p class="h4">{component.name}</p>
	</header>
	<div class="grid grid-cols-2 gap-4">
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
		<label class="label col-span-2">
			{#if component.driver === 'DUUIRemoteDriver'}
				<span>Target</span>
			{:else if component.driver === 'DUUIUIMADriver'}
				<span>Class Path</span>
			{:else}
				<span>Image Name</span>
			{/if}
			<input class="input" type="text" placeholder="Name" bind:value={component.target} />
		</label>
	</div>

	{#if expanded}
		<h3>Advanced Settings</h3>
		{#if component.driver === DUUIDockerDriver || component.driver === DUUISwarmDriver}
			<DockerSettings {component} />
		{:else if component.driver === DUUIRemoteDriver}
			<RemoteSettings {component} />
		{/if}
	{/if}
	<button
		class="btn-icon variant-soft-surface self-center pointer-events-auto"
		on:click={() => (expanded = !expanded)}
	>
		<Fa icon={expanded ? faCaretUp : faCaretDown} />
	</button>
</div>
