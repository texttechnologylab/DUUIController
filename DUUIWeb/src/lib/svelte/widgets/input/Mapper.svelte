<script lang="ts">
	import { createEventDispatcher } from 'svelte'
	import { faClose } from '@fortawesome/free-solid-svg-icons'
	import Text from './Text.svelte'
	import Fa from 'svelte-fa'

	const dispatcher = createEventDispatcher()

	export let map: Map<string, string> = new Map()

	let key: string = ''
	let value: string = ''

	export let label: string = 'Settings'

	const addKey = () => {
		if (key === '' || value === '') return
		map.set(key, value)
		key = ''
		value = ''
		map = new Map([...map.entries()].sort())

		dispatcher('update', { map: map })
	}

	const deleteKey = (key: string) => {
		map.delete(key)
		map = new Map([...map.entries()].sort())

		dispatcher('update', { map: map })
	}
</script>

<div class="md.grid md:grid-cols-3 md:gap-4 space-y-4 items-start">
	<div class="space-y-4">
		<Text
			label="Key"
			name="key"
			bind:value={key}
			on:keydown={(event) => {
				if (event.key !== 'Enter') return

				if (key && value) {
					addKey()
				}
			}}
		/>
		<Text
			label="Value"
			name="value"
			bind:value
			on:keydown={(event) => {
				if (event.key !== 'Enter') return

				if (key && value) {
					addKey()
				}
			}}
		/>
	</div>

	<div class="label col-span-2">
		<p class="uppercase text-xs tracking-widest">{label}</p>
		<div
			class="border-[1px] bg-white dark:bg-surface-600 border-surface-400/20 min-h-[120px] flex flex-wrap gap-2 p-2 items-start"
		>
			{#each map.entries() as [_key, _value]}
				<div class="chip variant-soft-primary py-0">
					<button
						class="space-x-4 py-2"
						on:click={() => {
							key = _key
							value = _value
						}}
					>
						<span class="border-r-2 border-r-primary-500/20 dark:border-r-primary-200/20 pr-4"
							>{_key}</span
						>
						<span
							class="border-r-2 border-r-primary-500/20 dark:border-r-primary-200/20 pr-4 font-bold"
							>{_value}</span
						>
					</button>
					<button class="px-4 py-2" on:click={() => deleteKey(_key)}>
						<Fa icon={faClose} size="xs" />
					</button>
				</div>
			{/each}
		</div>
	</div>
</div>
