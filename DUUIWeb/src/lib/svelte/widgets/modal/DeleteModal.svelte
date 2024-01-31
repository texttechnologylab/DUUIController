<script lang="ts">
	import { faClose } from '@fortawesome/free-solid-svg-icons'
	import { getModalStore } from '@skeletonlabs/skeleton'
	import Fa from 'svelte-fa'

	const modalStore = getModalStore()

	export let title: string = $modalStore[0].meta['title'] || 'Delete'
	export let message: string = $modalStore[0].meta['message'] || 'Are you sure?'
	export let deleteText: string = $modalStore[0].meta['deleteText'] || 'Delete'
</script>

<div class="z-50 bg-surface-50-900-token w-modal">
	<div
		class="p-4 flex items-center justify-between gap-4 border-b border-color bg-surface-100 dark:bg-surface-700"
	>
		<p class="h3 font-bold">{title}</p>
		<button
			on:click={() => modalStore.close()}
			class="text-surface-400 hover:text-surface-900 dark:hover:text-surface-100 transform-colors duration-300"
		>
			<Fa icon={faClose} scale={1.5} />
		</button>
	</div>
	<div class="p-8">
		<p>{message}</p>
	</div>
	<div class="p-4 px-8 grid grid-cols-2 items-center gap-4 justify-end">
		<button
			class="button-error button-modal"
			on:click={() => {
				if ($modalStore[0].response) {
					$modalStore[0]?.response(true)
					modalStore.close()
				}
			}}
		>
			<span>{deleteText}</span>
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
