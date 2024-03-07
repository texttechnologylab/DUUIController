<!--
	@component
	A component for managing json like key value pairs.
-->
<script lang="ts">
	import { showConfirmationModal } from '$lib/svelte/utils/modal'
	import { faCheck, faClose, faPen, faPlus } from '@fortawesome/free-solid-svg-icons'
	import { getModalStore } from '@skeletonlabs/skeleton'
	import Fa from 'svelte-fa'
	import TextInput from './TextInput.svelte'

	export let data: Map<string, any> = new Map()

	let edit: boolean = false
	let key: string = ''
	let value: string = ''
	export let label: string = ''

	const remove = async (key: string) => {
		const confirm = await showConfirmationModal(
			{
				title: 'Delete Parameter',
				message: `Please confirm the deletion of ${key}.`,
				textYes: 'Delete'
			},
			modalStore
		)

		if (confirm) {
			data.delete(key)
			data = data
		}
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

	const modalStore = getModalStore()
	const clearParameters = async () => {
		const confirm = await showConfirmationModal(
			{
				title: 'Clear all Parameters',
				message: 'Please confirm the deletion of ALL Parameters.',
				textYes: 'Yes, clear'
			},
			modalStore
		)

		if (confirm) {
			data.clear()
			data = data
		}
	}
</script>

<div class="label flex flex-col">
	{#if label}
		<span class="form-label">{label}</span>
	{/if}
	<div class=" space-y-4">
		<div class="flex items-center gap-2 mb-4">
			{#if !edit}
				<button
					class="button-neutral"
					on:click={() => {
						key = ''
						value = ''
						edit = true
					}}><Fa icon={faPlus} /><span>New</span></button
				>
				{#if data.size > 0}
					<button class="button-neutral" on:click={clearParameters}>
						<span>Clear All</span>
						<Fa icon={faClose} />
					</button>
				{/if}
			{:else}
				<div class="grid gap-1">
					<div class="flex-center-4">
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
					<div class="flex-center-4">
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
		{#if data.size === 0}
			<p>Click new, enter both a key and value then press enter or click confirm.</p>
		{/if}

		<div class="flex flex-wrap justify-start items-start gap-2">
			{#each data.entries() as [_key, _value]}
				<div class="input-wrapper p-4 min-w-[180px]">
					<div class="flex-center-4 justify-between">
						<p class="text-lg font-bold">{_value}</p>
						<button
							class="rounded-full hover:text-error-500 transition-colors"
							on:click={() => remove(_key)}><Fa icon={faClose} size="lg" /></button
						>
					</div>
					<div class="flex-center-4 justify-between">
						<p class="dimmed text-sm leading-tight">{_key}</p>
						<button
							class="aspect-square rounded-full hover:text-primary-500 transition-colors"
							on:click={() => {
								edit = true
								key = _key
								value = _value
							}}><Fa icon={faPen} size="sm" /></button
						>
					</div>
				</div>
			{/each}
		</div>
	</div>
</div>
