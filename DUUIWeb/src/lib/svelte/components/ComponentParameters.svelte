<script lang="ts">
	import { faPlus } from '@fortawesome/free-solid-svg-icons'
	import { createEventDispatcher } from 'svelte'
	import Fa from 'svelte-fa'

	const dispatcher = createEventDispatcher()

	export let parameters: Map<string, string> = new Map()

	export let label: string = ''
	let key: string = ''
	let value: string = ''

	const addKey = () => {
		if (key === '' || value === '') {
			error = { key: key === '', value: value === '' }

			return
		}
		parameters.set(key, value)
		key = ''
		value = ''
		parameters = new Map([...parameters.entries()].sort())

		dispatcher('update', { map: parameters })
	}

	const deleteKey = (key: string) => {
		parameters.delete(key)
		parameters = new Map([...parameters.entries()].sort())

		dispatcher('update', { map: parameters })
	}

	let error = {
		key: false,
		value: false
	}
</script>

<div class="label">
	{#if label}
		<span class="form-label">{label}</span>
	{/if}
	<div class="grid grid-cols-2 gap-4">
		<p>Key</p>
		<p>Value</p>
		{#if parameters.size > 0}
			{#each parameters.entries() as [_key, _value]}
				<input
					class="input-wrapper"
					bind:value={_key}
					on:keydown={(event) => {
						if (event.key === 'Enter') {
							addKey()
						}
					}}
				/>
				<input
					class=" input-wrapper"
					bind:value={_value}
					on:keydown={(event) => {
						if (event.key === 'Enter') {
							addKey()
						}
					}}
				/>
			{/each}
		{/if}
		<input
			class="{error.key ? 'input-error' : ''} input-wrapper"
			bind:value={key}
			on:keydown={(event) => {
				if (event.key === 'Enter') {
					addKey()
				}
			}}
		/>
		<input
			class="{error.key ? 'input-error' : ''} input-wrapper"
			bind:value
			on:keydown={(event) => {
				if (event.key === 'Enter') {
					addKey()
				}
			}}
		/>

		<div>
			<button class="button-primary" on:click={addKey}><Fa icon={faPlus} /> Add </button>
		</div>
	</div>
</div>
