<script lang="ts">
	import { createEventDispatcher } from 'svelte'
	import { faClose, faInfo, faPlus } from '@fortawesome/free-solid-svg-icons'
	import Text from './TextInput.svelte'
	import Fa from 'svelte-fa'
	import { popup, type PopupSettings } from '@skeletonlabs/skeleton'
	import ActionButton from '../action/ActionButton.svelte'

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

	const settingsInfo: PopupSettings = {
		event: 'click',
		target: 'settingsInfo',
		placement: 'right-start',
		middleware: {
			offset: 8
		}
	}
</script>

<div
	class="text-sm z-10 break-words max-w-[50ch] variant-filled-primary rounded-md p-4 shadow-lg
	space-y-4"
	data-popup="settingsInfo"
>
	<p>Enter both a key and value then press enter or click the button below.</p>
	<p>
		By setting the option
		<span class="italic font-bold">withDockerImageFetching</span>
		to true for a component, the Pipeline attempts to download the image from Docker Hub if it is not
		already present locally.
	</p>
</div>

<div class="space-y-4 items-start">
	<div class="gap-4 grid md:grid-cols-2">
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
	<div class="flex items-center gap-4">
		<ActionButton text="Add" icon={faPlus} disabled={!(!!key && !!value)} on:click={addKey} />
		{#if map.size === 0}
			<div
				class="btn-sm inline-flex rounded-full aspect-square variant-filled-primary cursor-pointer"
				use:popup={settingsInfo}
			>
				<Fa icon={faInfo} />
			</div>
		{/if}
	</div>

	{#if map.size > 0}
		<div class="label col-span-2">
			<p class="uppercase text-xs tracking-widest">{label}</p>
			<div
				class="rounded-md border-[1px] bg-gray dark:bg-surface-800 border-surface-400/20 flex flex-wrap gap-2 p-2 items-start"
			>
				{#each map.entries() as [_key, _value]}
					<div class="chip variant-soft-primary gap-2">
						<button
							class="flex flex-col items-start"
							on:click={() => {
								key = _key
								value = _value
							}}
						>
							<span>{_key}</span>
							<span class="font-bold">{_value}</span>
						</button>
						<button class="self-center" on:click={() => deleteKey(_key)}>
							<Fa icon={faClose} />
						</button>
					</div>
				{/each}
			</div>
		</div>
	{/if}
</div>
