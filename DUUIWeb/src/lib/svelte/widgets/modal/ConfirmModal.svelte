<script lang="ts">
	import { getModalStore } from '@skeletonlabs/skeleton'

	const modalStore = getModalStore()
	export let title: string = $modalStore[0].meta['title'] || ''
	export let message: string = $modalStore[0].meta['message'] || ''
	export let confirmText: string = $modalStore[0].meta['confirmText'] || 'Confirm'
</script>

<div class="z-50 bg-surface-50-900-token w-modal">
	<div
		class="p-4 flex items-center justify-between gap-4 border-b border-color bg-surface-100 dark:bg-surface-700"
	>
		<h3 class="h3">{title}</h3>
	</div>
	<div class="space-y-8">
		<div class="p-8">
			<p>{message}</p>
		</div>
		<div class="p-4 px-8 border-t border-color grid grid-cols-2 items-center gap-4 justify-end">
			<button
				class="button-error button-modal"
				on:click={() => {
					if ($modalStore[0].response) {
						$modalStore[0]?.response(true)
						modalStore.close()
					}
				}}
			>
				<span>{confirmText}</span>
			</button>
			<button
				class="button-primary button-modal"
				on:click={() => {
					if ($modalStore[0].response) {
						$modalStore[0]?.response(false)
						modalStore.close()
					}
				}}
			>
				<span>Cancel</span>
			</button>
		</div>
	</div>
</div>
