<script lang="ts">
	import { faArrowLeft, faTrash } from '@fortawesome/free-solid-svg-icons'
	import ActionButton from '../action/ActionButton.svelte'
	import { getModalStore } from '@skeletonlabs/skeleton'

	const modalStore = getModalStore()
	export let title: string = $modalStore[0].meta['title'] || 'Delete'
	export let body: string = $modalStore[0].meta['body'] || 'Are you sure?'
</script>

<div class="z-50 card p-4 items-start justify-start rounded-none shadow-lg space-y-2 w-modal">
	<p class="h4">{title}</p>
	<p>{body}</p>
	<div class="flex items-center gap-4 justify-end !mt-8">
		<ActionButton
			text="Cancel"
			icon={faArrowLeft}
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
