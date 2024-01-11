<script lang="ts">
	import {
		faCancel,
		faCheck,
		faClose,
		faFileImport,
		faPen,
		faPlus,
		faUndo
	} from '@fortawesome/free-solid-svg-icons'
	import Fa from 'svelte-fa'
	import TextInput from './TextInput.svelte'
	import { FileButton, getToastStore } from '@skeletonlabs/skeleton'

	export let data: Map<string, any> = new Map()
	export let nested: boolean = false

	let edit: boolean = false
	let key: string = ''
	let value: string = ''

	type Action = {
		key: string
		value: string
		method: 'create' | 'remove' | 'update'
	}

	let history: Action[] = []

	const undo = () => {
		if (history.length === 0) return
		const action: Action | undefined = history.pop()
		if (action === undefined) return

		history = history

		if (action.method === 'create') {
			data.delete(key)
		} else {
			data.set(action.key, action.value)
		}

		data = data
	}

	const remove = (key: string) => {
		history.push({ key: key, value: data.get(key), method: 'remove' })
		history = history

		data.delete(key)
		data = data
	}

	const create = () => {
		if (data.has(key)) {
			history.push({ key: key, value: data.get(key), method: 'update' })
		} else {
			history.push({ key: key, value: value, method: 'create' })
		}

		if (value === 'true') {
			data.set(key, true)
		} else if (value === 'false') {
			data.set(key, false)
		} else if (!isNaN(+value)) {
			data.set(key, +value)
		} else {
			data.set(key, value)
		}
		history = history
		data = data
		edit = false
	}

	let files: FileList
	const importData = async () => {
		if (!files) return
		const file: File = files[0]
		try {
			const importedData = JSON.parse(await file.text())
			data = new Map(Object.entries(importedData))
			for (let [key, value] of data.entries()) {
				if (typeof value === 'object') {
					data.delete(key)
				}
			}
		} catch (err) {
			getToastStore().trigger({
				message: 'Error'
			})
		}
	}
</script>

<div class="label">
	<span class="form-label">Settings</span>
	<div class="input-no-highlight p-4">
		<div class="flex items-center gap-2 mb-4">
			{#if !edit}
				<!-- {#if !nested}
					<FileButton
						bind:files
						name="importFile"
						button="btn variant-soft-primary"
						accept="application/json"
						on:change={importData}
					>
						<span>Import</span>
						<Fa icon={faFileImport} />
					</FileButton>
				{/if} -->

				<button
					class="btn variant-soft-primary"
					on:click={() => {
						key = ''
						value = ''
						edit = true
					}}><span>New</span><Fa icon={faPlus} size="lg" /></button
				>
				{#if history.length > 0}
					<button disabled={history.length === 0} class="btn variant-soft-primary" on:click={undo}>
						<span>Undo</span>
						<Fa icon={faUndo} />
					</button>
				{/if}
			{:else}
				<div class="flex items-center gap-4">
					<div>
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
					</div>
					<div class="flex flex-col items-start justify-center gap-4">
						<button
							disabled={!key || !value}
							class="aspect-square rounded-full {key && value
								? 'hover:text-success-500 transition-colors'
								: 'opacity-50'}"
							on:click={create}><Fa icon={faCheck} size="lg" /></button
						>
						<button
							class="aspect-square rounded-full hover:text-error-500 transition-colors"
							on:click={() => (edit = false)}><Fa icon={faClose} size="lg" /></button
						>
					</div>
				</div>
			{/if}
		</div>
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
		<div class="grid grid-cols-2 justify-start items-start gap-2">
			{#each data.entries() as [_key, _value], index}
				<div class="bg-fancy p-4 rounded-md">
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

				<!-- <div>
				<div class="flex items-center justify-start gap-2">
					<div class="flex flex-col gap-1">
						<div class="flex items-center gap-x-2">
							<button
								class="spect-square rounded-full hover:text-error-500 transition-colors"
								on:click={() => remove(_key)}><Fa icon={faClose} size="lg" /></button
							>
							<span class="text-sm text-primary-600">{typeof _value}</span>
						</div>

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
				</div> -->
			{/each}
		</div>
	</div>
</div>
