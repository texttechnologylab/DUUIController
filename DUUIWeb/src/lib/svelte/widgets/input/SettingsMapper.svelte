<script lang="ts">
	import { createEventDispatcher } from 'svelte'
	import IconButton from '../action/IconButton.svelte'
	import { faAdd, faClose, faTrash } from '@fortawesome/free-solid-svg-icons'
	import TextInput from './TextInput.svelte'

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

<div class="">
	<div class="">
		<div class="grid items-center gap-2 grow grid-cols-2">
			<TextInput name="key" bind:value={key} />
			<TextInput name="value" bind:value />
		</div>
		<IconButton
			_class={key === '' || value === '' ? 'hidden' : 'inline-flex'}
			icon={faAdd}
			on:click={addKey}
		/>
	</div>
	<div class="space-y-1">
		{#each settings.entries() as [key, value]}
			<div class="flex items-center gap-2">
				<div class="grid items-center gap-2 grow grid-cols-2">
					<p class="rounded-none variant-soft-surface border-0 p-2">{key}</p>
					<p class="rounded-none variant-soft-surface border-0 p-2">{value}</p>
				</div>
				<IconButton icon={faClose} variant="variant-soft-error" on:click={() => deleteKey(key)} />
			</div>
		{/each}
	</div>
</div>
