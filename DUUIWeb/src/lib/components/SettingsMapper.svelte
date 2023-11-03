<script lang="ts">
	import { createEventDispatcher } from 'svelte'
	import IconButton from './IconButton.svelte'
	import { faAdd, faClose, faTrash } from '@fortawesome/free-solid-svg-icons'

	const dispatcher = createEventDispatcher()

	export let settings: Map<string, string> = new Map()
	let key: string = ''
	let value: string = ''

	const addKey = () => {
		if (key === '' || value === '') return
		settings.set(key, value)
		key = ''
		value = ''
		settings = settings
	}

	const deleteKey = (key: string) => {
		settings.delete(key)
		settings = settings
	}
</script>

<h3>Settings</h3>
<div class="flex items-center gap-2 rounded-md mt-1 mb-2">
	<div class="grid items-center gap-2 rounded-md grow grid-cols-2">
		<div>
			<input
				placeholder="Key"
				class="variant-soft-surface border-0 rounded-md input"
				bind:value={key}
			/>
		</div>
		<div>
			<input
				placeholder="Value"
				class="variant-soft-surface border-0 rounded-md input"
				bind:value
			/>
		</div>
	</div>
	<IconButton icon={faAdd} on:click={addKey} />
</div>
<div class="space-y-1">
	{#each settings.entries() as [key, value]}
		<div class="flex items-center gap-2 rounded-md">
			<div class="grid items-center gap-2 rounded-md grow grid-cols-2">
				<p class="variant-soft-surface border-0 rounded-md p-2 input">{key}</p>
				<p class="variant-soft-surface border-0 rounded-md p-2 input">{value}</p>
			</div>
			<IconButton icon={faClose} variant="variant-soft-error" on:click={() => deleteKey(key)} />
		</div>
	{/each}
</div>
