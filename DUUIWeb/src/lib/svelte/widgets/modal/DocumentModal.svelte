<script lang="ts">
	import {
		Output,
		type DUUIDocument,
		type DUUIDocumentInput,
		type DUUIDocumentOutput,
		Input,
		isCloudProvider
	} from '$lib/duui/io'
	import Anchor from '$lib/svelte/widgets/action/Anchor.svelte'
	import { datetimeToString, equals, formatFileSize } from '$lib/duui/utils/text'
	import { formatMilliseconds } from '$lib/duui/utils/time'
	import {
		faCheckDouble,
		faFileDownload,
		faFileUpload,
		faWarning
	} from '@fortawesome/free-solid-svg-icons'
	import { getModalStore } from '@skeletonlabs/skeleton'
	import Fa from 'svelte-fa'
	import Chart from 'svelte-frappe-charts'
	import Search from '../input/Search.svelte'
	import { onMount } from 'svelte'
	import { scrollIntoView } from '$lib/duui/utils/ui'

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

	onMount(() => {
		scrollIntoView('scroll-top')
	})
</script>

<div class="section-wrapper container max-w-6xl sticky top-0 max-h-[80vh] !overflow-y-auto">
	<div id="scroll-top" />
	<div class="space-y-4">
		<div
			class="font-bold text-2xl p-4 border-b border-surface-200 dark:border-surface-500 text-center flex items-center justify-between sticky top-0 z-10 bg-fancy-solid"
		>
			<p>{document.name}</p>
			{#if document.error}
				<Fa icon={faWarning} size="lg" class="text-error-500" />
			{:else if document.finished}
				<Fa icon={faCheckDouble} size="lg" class="text-success-500" />
			{/if}
		</div>

		<div class="p-4 space-y-8">
			{#if document.error}
				<p class="section-wrapper text-lg text-error-500 font-bold p-4">
					{document.error}
				</p>
			{/if}
			<div class="grid grid-cols-3 justify-center items-center section-wrapper text-sm">
				<div
					class="bg-fancy flex flex-col items-start justify-center gap-2 border-r border-surface-200 dark:border-surface-500 p-4"
				>
					<p class="font-bold">Status</p>
					<p>{document.status}</p>
				</div>
				<div
					class="bg-fancy flex flex-col items-start justify-center gap-2 border-r border-surface-200 dark:border-surface-500 p-4"
				>
					<p class="font-bold">Source</p>
					<p>{input.source}</p>
				</div>
				<div class="bg-fancy flex flex-col items-start justify-center gap-2 p-4">
					<p class="font-bold">Size</p>
					<p>{document.size ? formatFileSize(document.size) : 'Unknown'}</p>
				</div>
			</div>
			<div class="grid grid-cols-3 justify-center items-center section-wrapper rounded-md text-sm">
				<div
					class="bg-fancy flex flex-col items-start justify-center gap-2 border-r border-surface-200 dark:border-surface-500 p-4"
				>
					<p class="font-bold">Setup</p>
					<p>{formatMilliseconds(document.decodeDuration + document.deserializeDuration)}</p>
				</div>
				<div
					class="bg-fancy flex flex-col items-start justify-center gap-2 border-r border-surface-200 dark:border-surface-500 p-4"
				>
					<p class="font-bold">Wait</p>
					<p>{formatMilliseconds(document.waitDuration)}</p>
				</div>
				<div class="bg-fancy flex flex-col items-start justify-center gap-2 p-4">
					<p class="font-bold">Process</p>
					<p>{formatMilliseconds(document.processDuration)}</p>
				</div>
				<p
					class="bg-fancy col-span-3 border-t border-surface-200 dark:border-surface-500 p-4 text-center font-bold text-lg"
				>
					Total {formatMilliseconds(totalDuration)}
				</p>
			</div>
			{#if document.annotations}
				<div class="section-wrapper text-sm">
					{#each Object.entries(document.annotations) as entry}
						<div class="grid grid-cols-2 border-b border-surface-200 dark:border-surface-500">
							<div class="bg-fancy flex flex-col items-start justify-center gap-2 p-4">
								<p class="font-bold">Class</p>
								<p class="">{entry[0].split('.').slice(-1)}</p>
							</div>
							<div class="bg-fancy flex flex-col items-start justify-center gap-2 p-4">
								<p class="font-bold">Count</p>
								<p>{entry[1]}</p>
							</div>
							<div class="bg-fancy flex flex-col items-start justify-center gap-2 p-4 col-span-2">
								<p class="font-bold">Annotation</p>
								<p class="">{entry[0]}</p>
							</div>
						</div>
					{/each}
					<!-- <pre class="text-sm">{JSON.stringify(document.annotations, null, 2)}</pre> -->
				</div>
			{/if}

			<!-- <Chart
					{data}
					type="pie"
					title="Processing Step Duration (ms)"
					colors={['#006c98', '#4d98b7', '#006189', '#005172']}
				/> -->
		</div>

		<div
			class="flex justify-center gap-4 text-center border-t border-surface-200 dark:border-surface-500 p-8"
		>
			{#if isCloudProvider(input.source)}
				<a href={URLIn} target="_blank" class="anchor">Input Source</a>
			{/if}
			{#if isCloudProvider(output.target)}
				<a href={URLOut} target="_blank" class="anchor">Output Target</a>
			{/if}
		</div>
	</div>
</div>
