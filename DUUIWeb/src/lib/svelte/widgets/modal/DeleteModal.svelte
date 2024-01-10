<script lang="ts">
	import { faArrowLeft, faClose, faTrash, faWarning } from '@fortawesome/free-solid-svg-icons'
	import ActionButton from '../action/ActionButton.svelte'
	import { getModalStore } from '@skeletonlabs/skeleton'
	import Fa from 'svelte-fa'

	const modalStore = getModalStore()
	export let title: string = $modalStore[0].meta['title'] || 'Delete'
	export let body: string = $modalStore[0].meta['body'] || 'Are you sure?'
</script>

<div
	class="z-50 overflow-hidden rounded-md dark:bg-surface-700 shadow-lg border-surface-400/20
	 w-modal"
>
	<div
		class=" p-4 variant-filled-error dark:variant-soft-error flex items-center justify-between gap-4"
	>
		<p class="h3 font-bold">{title}</p>
		<Fa icon={faWarning} size="2x" />
	</div>
	<div class="p-4 bg-white dark:bg-surface-600 space-y-16">
		<div>
			<p>{body}</p>
		</div>
		<div class="flex items-center gap-4 justify-end">
			<ActionButton
				text="Confirm"
				variant="dark:variant-soft-error variant-filled-error"
				icon={faTrash}
				on:click={() => {
					if ($modalStore[0].response) {
						$modalStore[0]?.response(true)
						modalStore.close()
					}
				}}
			/>
			<ActionButton
				text="Cancel"
				icon={faClose}
				on:click={() => {
					if ($modalStore[0].response) {
						$modalStore[0]?.response(false)
						modalStore.close()
					}
				}}
			/>
		</div>
	</div>
</div>
