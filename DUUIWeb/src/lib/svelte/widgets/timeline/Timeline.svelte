<script lang="ts">
	import type { DUUIDocument } from '$lib/duui/io'
	import type { DUUIProcess } from '$lib/duui/process'
	import { formatMilliseconds } from '$lib/duui/utils/time'
	import { getModalStore, type ModalComponent, type ModalSettings } from '@skeletonlabs/skeleton'
	import DocumentModal from '../modal/DocumentModal.svelte'

	export let process: DUUIProcess
	export let documents: DUUIDocument[] = []

	let start = 0
	let end = 100

	let container: HTMLDivElement

	let maximumEndTime = Math.max(...documents.map((d) => d.endTime)) || process.endTime

	function calculateWidth(document: DUUIDocument) {
		return ((document.endTime - document.startTime) / (endTime - process.startTime)) * 100 + '%'
	}

	let interval: number = 10
	$: markers = Array.from(
		{ length: Math.ceil((end - start) / interval) + 1 },
		(_, index) => start + index * interval
	)

	$: endTime = maximumEndTime >= process.startTime ? maximumEndTime : new Date().getTime() + 1000
	let hoverIndex: number | undefined = undefined

	const modalStore = getModalStore()
	const modalComponent: ModalComponent = {
		ref: DocumentModal,
		props: { input: process.input, output: process.output }
	}

	const showDocumentModal = (document: DUUIDocument) => {
		const modal: ModalSettings = {
			type: 'component',
			component: modalComponent,
			meta: { document: document }
		}
		modalStore.trigger(modal)
	}
</script>

<div
	class=" hidden sm:block container section-wrapper 
	mx-auto p-8 pb-16 space-y-4"
>
	<h2 class="h2">Timeline</h2>
	<div class="w-full flex gap-2 mb-8">
		<div>
			{#each documents as document, index}
				{#if index > 0}
					<div class="relative border-t border-dashed w-full border-transparent" />
				{/if}
				<button
					class="btn-sm pl-0 block text-left my-1 border-r-8 border-transparent rounded-none {index ===
					hoverIndex
						? document.error
							? '!border-r-error-500'
							: '!border-r-primary-500'
						: ''}"
					on:click={() => showDocumentModal(document)}
					on:mouseenter={() => (hoverIndex = index)}
					on:mouseleave={() => (hoverIndex = undefined)}
				>
					{document.name}
				</button>
			{/each}
		</div>
		<div class="grow">
			<div bind:this={container} class="relative">
				{#each documents as document, index}
					{#if index > 0}
						<div class="relative border-t border-dashed w-full border-surface-400/50" />
					{/if}
					{#if container}
						<button
							class="btn-sm block {document.error
								? 'variant-glass-error'
								: 'variant-glass-primary'} {index === hoverIndex
								? document.error
									? '!bg-error-500'
									: '!bg-primary-500'
								: ''} rounded-md text-sm my-1 text-transparent"
							style="transform: translate({((document.startTime - process.startTime) /
								(endTime - process.startTime)) *
								container.clientWidth}px, 0); width: {calculateWidth(document)}"
							on:click={() => showDocumentModal(document)}
							on:mouseenter={() => (hoverIndex = index)}
							on:mouseleave={() => (hoverIndex = undefined)}
							>{formatMilliseconds(document.endTime - document.startTime)}</button
						>
					{/if}
				{/each}
				{#each markers as marker, index}
					{#if index === 0}
						<p
							class="absolute -bottom-6 text-xs -translate-x-1/2 whitespace-nowrap"
							style="left: {index * interval}%"
						>
							{0}s
						</p>
					{:else if index === markers.length - 1}
						<p
							class="absolute -bottom-6 text-xs -translate-x-1/2 whitespace-nowrap"
							style="left: {index * interval}%"
						>
							{formatMilliseconds(endTime - process.startTime)}
						</p>
					{:else}
						<div
							class="absolute h-full border-l top-0 border-l-surface-400/50 dark:border-l-white -z-10 border-dashed"
							style="left: {index * interval}%"
						/>
						<p
							class="absolute -bottom-6 text-xs -translate-x-1/2 whitespace-nowrap"
							style="left: {index * interval}%"
						>
							{formatMilliseconds((index * (endTime - process.startTime)) / interval)}
						</p>
					{/if}
				{/each}
			</div>
		</div>
	</div>
</div>
