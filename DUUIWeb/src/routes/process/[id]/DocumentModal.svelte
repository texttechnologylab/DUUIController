<script lang="ts">
	import {
		Output,
		type DUUIDocument,
		type DUUIDocumentInput,
		type DUUIDocumentOutput,
		Input
	} from '$lib/duui/io'
	import { equals, formatFileSize } from '$lib/utils/text'
	import { formatSeconds } from '$lib/utils/time'
	import {
		faCheckDouble,
		faFile,
		faFileDownload,
		faFileUpload
	} from '@fortawesome/free-solid-svg-icons'
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
</script>

{#if $modalStore[0]}
	<div class="container mx-auto max-w-2xl">
		<div class="space-y-8 card shadow-lg rounded-md p-4">
			<div class="flex justify-start items-center gap-8">
				{#if document.done}
					<Fa icon={faCheckDouble} size="2x" />
				{/if}
				<h3 class="h3">{document.name}</h3>
				{#if document.size}
					<p class="text-2xl ml-auto">{formatFileSize(document.size)}</p>
				{/if}
			</div>

			{#if document.processDuration > 0}
				<div class="grid grid-cols-3 gap-4 text-center">
					<p class="variant-soft-surface rounded-md p-2">Decode {document.decodeDuration} ms</p>
					<p class="variant-soft-surface rounded-md p-2">
						Deserialize {document.deserializeDuration} ms
					</p>
					<p class="variant-soft-surface rounded-md p-2">Process {document.processDuration} ms</p>
				</div>
			{/if}

			<div class="flex justify-center gap-4 text-center">
				<a href={URLIn} target="_blank" class="btn variant-ghost-primary rounded-md">Source File</a>
				<a href={URLOut} target="_blank" class="btn variant-ghost-primary rounded-md">Target File</a
				>
			</div>
		</div>
	</div>
{/if}
