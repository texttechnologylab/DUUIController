<script lang="ts">
	import {
		faCancel,
		faCheck,
		faClose,
		faFileImage,
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

<div class="form-no-highlight">
	<div class="flex items-center gap-2 mb-4">
		{#if !edit}
			{#if !nested}
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
			{/if}

			<button
				class="btn variant-soft-primary"
				on:click={() => {
					key = ''
					value = ''
					edit = true
				}}><span>New</span><Fa icon={faPlus} size="lg" /></button
			>
			<button disabled={history.length === 0} class="btn variant-soft-primary" on:click={undo}>
				<span>Undo</span>
				<Fa icon={faUndo} />
			</button>
		{:else}
			<div class="grid grid-cols-2 gap-4">
				<TextInput bind:value={key} placeholder="Key" />
				<TextInput bind:value placeholder="Value" />
				<button disabled={!key || !value} class="btn variant-soft-success" on:click={create}
					><span>Confirm</span><Fa icon={faCheck} size="lg" /></button
				>
				<button class="btn variant-soft-error" on:click={create}
					><span>Cancel</span><Fa icon={faCancel} size="lg" /></button
				>
			</div>
		{/if}
	</div>
	<div class="grid grid-cols-2 justify-start items-start gap-8">
		{#each data.entries() as [_key, _value], index}
			<div class="space-y-2 border-l-[1rem] border-l-primary-500/20 pl-2">
				<div class="flex items-center justify-between gap-4">
					<span class="text-sm text-primary-600">{typeof _value}</span>
					<button
						class="spect-square rounded-full hover:text-error-500 transition-colors"
						on:click={() => remove(_key)}><Fa icon={faClose} size="lg" /></button
					>
				</div>
				<div class="flex items-center justify-between gap-4">
					<span class="text-start">
						{_key}
					</span>
				</div>
				<div class="flex items-center justify-between gap-4">
					<span class="text-start">
						{_value}
					</span>
					<button
						class="aspect-square rounded-full hover:text-primary-500 transition-colors"
						on:click={() => {
							edit = true
							key = _key
							value = _value
						}}><Fa icon={faPen} /></button
					>
				</div>
			</div>
		{/each}
	</div>
</div>
