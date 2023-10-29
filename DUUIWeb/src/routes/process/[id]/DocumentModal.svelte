<script lang="ts">
	import {
		Output,
		type DUUIDocument,
		type DUUIDocumentInput,
		type DUUIDocumentOutput,
		Input,
		isCloudProvider
	} from '$lib/duui/io'
	import { equals, formatFileSize } from '$lib/utils/text'
	import { formatMilliseconds } from '$lib/utils/time'
	import { faCheckDouble, faX } from '@fortawesome/free-solid-svg-icons'
	import { getModalStore } from '@skeletonlabs/skeleton'
	import Fa from 'svelte-fa'

	export let input: DUUIDocumentInput
	export let output: DUUIDocumentOutput
	const modalStore = getModalStore()

	let document: DUUIDocument = $modalStore[0].meta.document

	let URLIn: string = ''
	let URLOut: string = ''

	if (equals(input.source, Input.Dropbox)) {
		if (input.folder.startsWith('/')) {
			URLIn = `https://www.dropbox.com/home/Apps/Cedric%20Test%20App${input.folder}/${document.name}`
		} else {
			URLIn = `https://www.dropbox.com/home/Apps/Cedric%20Test%20App/${input.folder}/${document.name}`
		}
	} else if (equals(input.source, Input.Minio)) {
		URLIn = `http://127.0.0.1:9090/browser/${input.folder}`
	} else {
		URLIn = ''
	}

	if (equals(output.target, Output.Dropbox)) {
		if (output.folder.startsWith('/')) {
			URLOut = `https://www.dropbox.com/home/Apps/Cedric%20Test%20App${output.folder}/${document.name}`
		} else {
			URLOut = `https://www.dropbox.com/home/Apps/Cedric%20Test%20App/${output.folder}/${document.name}`
		}
	} else if (equals(output.target, Output.Minio)) {
		URLOut = `http://127.0.0.1:9090/browser/${output.folder}`
	} else {
		URLOut = ''
	}

	let totalDuration: number =
		document.decodeDuration +
		document.deserializeDuration +
		document.processDuration +
		document.waitDuration

	let container: HTMLDivElement
	let width: number = 50

	$: {
		if (container) {
			width = container.offsetWidth
		}
	}
</script>

{#if $modalStore[0]}
	<div class="container mx-auto max-w-2xl">
		<div class="space-y-8 card shadow-lg rounded-md p-4">
			<div class="flex justify-start items-center gap-8">
				{#if document.error}
					<Fa icon={faX} size="2x" />
				{:else if document.finished}
					<Fa icon={faCheckDouble} size="2x" />
				{/if}
				<h3 class="h3">{document.name}</h3>
				{#if document.size}
					<p class="text-2xl ml-auto">{formatFileSize(document.size)}</p>
				{/if}
			</div>
			{#if document.error}
				<p class="text-error-400">{document.error}</p>
			{/if}

			<div class="grid grid-cols-2 gap-4 text-center">
				<p class="variant-soft-surface rounded-md p-2">
					Decode {formatMilliseconds(document.decodeDuration)}
				</p>
				<p class="variant-soft-surface rounded-md p-2">
					Deserialize {formatMilliseconds(document.deserializeDuration)}
				</p>
				<p class="variant-soft-surface rounded-md p-2">
					Wait {formatMilliseconds(document.waitDuration)}
				</p>
				<p class="variant-soft-surface rounded-md p-2">
					Process {formatMilliseconds(document.processDuration)}
				</p>
				<p class="variant-soft-surface rounded-md p-2 col-span-2">
					Total {formatMilliseconds(totalDuration)}
				</p>
			</div>

			<div class="flex justify-center gap-4 text-center">
				{#if isCloudProvider(input.source)}
					<a href={URLIn} target="_blank" class="btn variant-ghost-primary rounded-md"
						>Source File</a
					>
				{/if}
				{#if isCloudProvider(output.target)}
					<a href={URLOut} target="_blank" class="btn variant-ghost-primary rounded-md"
						>Target File</a
					>
				{/if}
			</div>
		</div>
	</div>
{/if}
