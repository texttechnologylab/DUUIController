<script lang="ts">
	import { createEventDispatcher } from 'svelte'
	import IconButton from '../action/IconButton.svelte'
	import { faAdd, faClose } from '@fortawesome/free-solid-svg-icons'
	import ActionButton from '../action/ActionButton.svelte'
	import Text from './Text.svelte'

	const dispatcher = createEventDispatcher()

	export let label: string = 'Settings'
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

<div class="space-y-1">
	<p class="label uppercase text-xs tracking-widest">{label}</p>

	<div class="space-y-2 p-4 border-[1px] border-surface-400/20">
		<div class="grid items-center gap-2 grow grid-cols-2">
			<Text label="Key" name="key" bind:value={key} />
			<Text label="Value" name="value" bind:value />
		</div>
		<ActionButton text="Add" disabled={key === '' || value === ''} icon={faAdd} on:click={addKey} />

		<div class="space-y-1">
			{#each settings.entries() as [key, value]}
				<div class="flex items-center gap-2">
					<div class="grid items-center gap-2 grow grid-cols-2">
						<p class="rounded-none border-[1px] border-surface-400/20 p-2">{key}</p>
						<p class="rounded-none border-[1px] border-surface-400/20 p-2">{value}</p>
					</div>
					<IconButton
						_class="w-8"
						icon={faClose}
						variant="variant-filled-error dark:variant-soft-error"
						on:click={() => deleteKey(key)}
					/>
				</div>
			{/each}
		</div>
	</div>
</div>
