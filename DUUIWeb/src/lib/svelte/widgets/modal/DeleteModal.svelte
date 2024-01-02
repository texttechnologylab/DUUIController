<script lang="ts">
	import { faArrowLeft, faClose, faTrash } from '@fortawesome/free-solid-svg-icons'
	import ActionButton from '../action/ActionButton.svelte'
	import { getModalStore } from '@skeletonlabs/skeleton'

	const modalStore = getModalStore()
	export let title: string = $modalStore[0].meta['title'] || 'Delete'
	export let body: string = $modalStore[0].meta['body'] || 'Are you sure?'
</script>

<div
	class="z-50 border-2 bg-white dark:bg-surface-700 shadow-lg border-surface-400/20 p-4 items-start justify-start rounded-none space-y-2 w-modal"
>
	<p class="h3">{title}</p>
	<p>{body}</p>
	<div class="flex items-center gap-4 justify-end !mt-8">
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
		<ActionButton
			text="Delete"
			variant="dark:variant-soft-error variant-filled-error"
			icon={faTrash}
			on:click={() => {
				if ($modalStore[0].response) {
					$modalStore[0]?.response(true)
					modalStore.close()
				}
			}}
		/>
	</div>
</div>
