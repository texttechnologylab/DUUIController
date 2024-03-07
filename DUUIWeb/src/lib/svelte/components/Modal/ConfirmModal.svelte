<!--
	@component
	A modal component that can be used to make the user confirm an action like deleting something.
-->
<script lang="ts">
	import { faClose } from '@fortawesome/free-solid-svg-icons'
	import { getModalStore } from '@skeletonlabs/skeleton'
	import Fa from 'svelte-fa'

	const modalStore = getModalStore()
	export let title: string = $modalStore[0].meta['title'] || ''
	export let message: string = $modalStore[0].meta['message'] || ''
	export let textYes: string = $modalStore[0].meta['textYes'] || 'Confirm'
	export let textNo: string = $modalStore[0].meta['textNo'] || 'Cancel'
</script>

<div class="z-50 bg-surface-50-900-token w-modal rounded-md overflow-hidden border border-color">
	<div class="modal-header bg-surface-100-800-token">
		<h3 class="h3">{title}</h3>
		<button on:click={modalStore.close}>
			<Fa icon={faClose} size="lg" />
		</button>
	</div>
	<div class="modal-body">
		<div class="p-8 space-y-8">
			<p>{message}</p>
			<div class="modal-footer">
				<div class="flex-center-4 justify-start">
					<button
						class="button-neutral button-modal hover:!variant-filled-error"
						on:click={() => {
							if ($modalStore[0].response) {
								$modalStore[0]?.response(true)
								modalStore.close()
							}
						}}
					>
						<span>{textYes}</span>
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
						<span>{textNo}</span>
					</button>
				</div>
			</div>
		</div>
	</div>

	<!-- <div class="space-y-8">
		
		<div class="p-4 px-8 border-t border-color grid grid-cols-2 items-center gap-4 justify-end"> -->
</div>
