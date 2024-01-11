<script lang="ts">
	import {
		faCheck,
		faClose,
		faEnvelope,
		faTrash,
		faWarning
	} from '@fortawesome/free-solid-svg-icons'
	import ActionButton from '../action/ActionButton.svelte'
	import { getModalStore } from '@skeletonlabs/skeleton'
	import Fa from 'svelte-fa'
	import TextInput from '../input/TextInput.svelte'

	const modalStore = getModalStore()
	export let title: string = $modalStore[0].meta['title']
	export let body: string = $modalStore[0].meta['body']
	let value: string = $modalStore[0].meta['value']
</script>

<div
	class="z-50 overflow-hidden rounded-md dark:bg-surface-700 shadow-lg border-surface-400/20
	 w-modal card"
>
	<div class="p-4 flex items-center justify-between gap-4">
		<p class="h3 font-bold">{title}</p>
		<Fa icon={faEnvelope} size="2x" />
	</div>
	<div class="p-4 bg-white dark:bg-surface-600 space-y-4">
		<p>{body}</p>
		<TextInput bind:value required />
		<div class="flex items-center gap-4 justify-end">
			<ActionButton
				disabled={!value}
				text="Confirm"
				icon={faCheck}
				on:click={() => {
					if ($modalStore[0].response) {
						$modalStore[0]?.response(value)
						modalStore.close()
					}
				}}
			/>
			<ActionButton
				text="Cancel"
				variant="dark:variant-soft-error variant-filled-error"
				icon={faClose}
				on:click={() => {
					if ($modalStore[0].response) {
						$modalStore[0]?.response('')
						modalStore.close()
					}
				}}
			/>
		</div>
	</div>
</div>
