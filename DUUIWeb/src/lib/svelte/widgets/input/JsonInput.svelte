<script lang="ts">
	import { faCheck, faClose, faPen, faPlus, faUndo } from '@fortawesome/free-solid-svg-icons'
	import Fa from 'svelte-fa'
	import TextInput from './TextInput.svelte'

	export let data: Map<string, any> = new Map()

	let edit: boolean = false
	let key: string = ''
	let value: string = ''

	type Action = {
		key: string
		value: string
		method: 'create' | 'remove' | 'update'
	}

	const remove = (key: string) => {
		data.delete(key)
		data = data
	}

	const create = () => {
		if (value === 'true') {
			data.set(key, true)
		} else if (value === 'false') {
			data.set(key, false)
		} else if (!isNaN(+value)) {
			data.set(key, +value)
		} else {
			data.set(key, value)
		}
		data = data
		edit = false
	}
</script>

<div class="label flex flex-col">
	<span class="form-label">Settings</span>
	<div class="input-no-highlight p-4 space-y-4">
		{#if data.size === 0}
			<div class="text-sm max-w-[60ch] space-y-4">
				<p>Click new, then enter both a key and value then press enter or click confirm.</p>
				<p>
					By setting the option
					<span class="italic font-bold">withDockerImageFetching</span>
					to true for a component, the Pipeline attempts to download the image from Docker Hub if it
					is not already present locally.
				</p>
			</div>
		{/if}
		<div class="flex items-center gap-2 mb-4">
			{#if !edit}
				<button
					class="button button-primary"
					on:click={() => {
						key = ''
						value = ''
						edit = true
					}}><span>New</span><Fa icon={faPlus} /></button
				>
				{#if data.size > 0}
					<button
						class="button button-error"
						on:click={() => {
							data.clear()
							data = data
						}}
					>
						<span>Clear All</span>
						<Fa icon={faClose} />
					</button>
				{/if}
			{:else}
				<div class="grid items-center">
					<div class="flex items-center gap-4">
						<TextInput
							bind:value={key}
							placeholder="Key"
							on:keydown={(event) => {
								if (event.key !== 'Enter') return
								if (key && value) {
									create()
								}
							}}
						/>
						<button
							disabled={!key || !value}
							class="aspect-square rounded-full {key && value
								? 'hover:text-success-500 transition-colors'
								: 'opacity-50'}"
							on:click={create}><Fa icon={faCheck} size="lg" /></button
						>
					</div>
					<div class="flex items-center gap-4">
						<TextInput
							bind:value
							placeholder="Value"
							on:keydown={(event) => {
								if (event.key !== 'Enter') return
								if (key && value) {
									create()
								}
							}}
						/>
						<button
							class="aspect-square rounded-full hover:text-error-500 transition-colors"
							on:click={() => (edit = false)}><Fa icon={faClose} size="lg" /></button
						>
					</div>
				</div>
			{/if}
		</div>

		<div class="flex flex-wrap justify-start items-start gap-2">
			{#each data.entries() as [_key, _value]}
				<div class="bg-fancy rounded-md p-4 border border-color">
					<div class="flex items-center gap-2">
						<button
							class="spect-square rounded-full hover:text-error-500 transition-colors"
							on:click={() => remove(_key)}><Fa icon={faClose} size="lg" /></button
						>
						<span class="text-sm text-primary-600">{typeof _value}</span>
					</div>
					<div class="flex items-center gap-2">
						<button
							class="aspect-square rounded-full hover:text-primary-500 transition-colors"
							on:click={() => {
								edit = true
								key = _key
								value = _value
							}}><Fa icon={faPen} /></button
						>

						<span class="text-start">
							{_key}
						</span>
						<span class="text-start">
							{_value}
						</span>
					</div>
				</div>
			{/each}
		</div>
	</div>
</div>
