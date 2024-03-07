<!--
	@component
	A modal component that displays a text input and asks for a value to be entered.
-->
<script lang="ts">
	import { faClose } from '@fortawesome/free-solid-svg-icons'
	import { getModalStore } from '@skeletonlabs/skeleton'
	import Fa from 'svelte-fa'
	import TextInput from '../Input/TextInput.svelte'

	const modalStore = getModalStore()
	export let title: string = $modalStore[0].meta['title']
	export let message: string = $modalStore[0].meta['message']
	export let textYes: string = $modalStore[0].meta['textYes'] || 'Copy'
	export let textNo: string = $modalStore[0].meta['textNo'] || 'Cancel'
	let value: string = $modalStore[0].meta['value']
</script>

<div class="z-50 bg-modal w-modal">
	<div
		class="p-4 flex items-center justify-between gap-4 border-b border-color bg-surface-100 dark:bg-surface-700"
	>
		<p class="h3">{title}</p>
		<button
			on:click={() => modalStore.close()}
			class="text-surface-400 hover:text-surface-900 dark:hover:text-surface-100 transform-colors duration-300"
		>
			<Fa icon={faClose} scale={1.5} />
		</button>
	</div>
	<div class="space-y-8">
		<div class="p-8 space-y-2">
			<p>{message}</p>
			<TextInput bind:value required />
		</div>
		<div class="p-4 px-8 border-t border-color grid grid-cols-2 items-center gap-4 justify-end">
			<button
				disabled={!value}
				class="button-primary button-modal"
				on:click={() => {
					if ($modalStore[0].response) {
						$modalStore[0]?.response(value)
						modalStore.close()
					}
				}}
			>
				<span>{textYes}</span>
			</button>
			<button
				class="button-neutral button-modal hover:!variant-filled-error"
				on:click={() => {
					if ($modalStore[0].response) {
						$modalStore[0]?.response('')
						modalStore.close()
					}
				}}
			>
				<span>{textNo}</span>
			</button>
		</div>
	</div>
</div>
