<script lang="ts">
	import { DropboxAppURL } from '$lib/config'
	import { IO, type DUUIDocument, type DUUIDocumentProvider } from '$lib/duui/io'
	import type { DUUIProcess } from '$lib/duui/process'
	import { formatFileSize, includes } from '$lib/duui/utils/text'
	import { formatMilliseconds } from '$lib/duui/utils/time'
	import { getStatusIcon, scrollIntoView } from '$lib/duui/utils/ui'
	import { userSession } from '$lib/store'
	import { faChevronUp, faClose } from '@fortawesome/free-solid-svg-icons'
	import { getModalStore } from '@skeletonlabs/skeleton'
	import { onMount } from 'svelte'
	import Fa from 'svelte-fa'
	import {
		getAnnotationsPlotOptions,
		getTimelinePlotOptions
	} from '../../../routes/processes/[oid]/chart'
	import Number from './Number.svelte'
	import Search from './Search.svelte'
	import type { DUUIPipeline } from '$lib/duui/pipeline'

	export let input: DUUIDocumentProvider
	export let output: DUUIDocumentProvider

	const modalStore = getModalStore()

	let document: DUUIDocument = $modalStore[0].meta.document
	let process: DUUIProcess = $modalStore[0].meta.process
	let pipeline: DUUIPipeline = $modalStore[0].meta.pipeline

	let URLIn: string = ''
	let URLOut: string = ''

	let annotationsExpanded: boolean = true
	let searchText: string = ''
	let minimumCount: number = 1
	let annotationFilter: Map<string, number> = new Map(Object.entries(document.annotations || {}))

	switch (input.provider) {
		case 'Dropbox':
			URLIn = DropboxAppURL + '/' + document.name
			break
		case 'Minio':
			URLIn = $userSession?.connections.minio.endpoint || ''
			break
		default:
			URLIn = ''
	}

	switch (output.provider) {
		case 'Dropbox':
			URLOut =
				DropboxAppURL +
				output.path +
				'/' +
				document.name.replace('.' + document.name.split('.').at(-1), output.file_extension)
			break
		case 'Minio':
			URLOut = $userSession?.connections.minio.endpoint || ''
			break
		default:
			URLOut = ''
	}
	let ApexCharts
	let loaded: boolean = false

	const chart = (node: HTMLDivElement, options: any) => {
		if (!loaded) return

		let _chart = new ApexCharts(node, options)
		_chart.render()

		return {
			update(options: any) {
				_chart.updateOptions(options)
			},
			destroy() {
				_chart.destroy()
			}
		}
	}

	$: options = getAnnotationsPlotOptions(annotationFilter)
	$: eventOptions = getTimelinePlotOptions(process, pipeline, document)

	onMount(() => {
		scrollIntoView('scroll-top')
		async function loadApexCharts() {
			const module = await import('apexcharts')
			ApexCharts = module.default
			window.ApexCharts = ApexCharts
			loaded = true
		}

		loadApexCharts()
	})

	$: {
		if (document.annotations) {
			annotationFilter = new Map(
				Object.entries(document.annotations).filter(
					(entry) =>
						(includes(entry[0], searchText) || searchText === '') && entry[1] >= minimumCount
				)
			)
		}
	}
</script>

<div
	class="section-wrapper bg-surface-50-900-token w-full md:max-w-[80%] sticky top-0 h-[90vh] !overflow-y-auto"
>
	<div id="scroll-top" />
	<div
		class="font-bold text-2xl p-4 border-surface-200 dark:border-surface-500 flex items-center justify-start gap-4 sticky top-0 z-10 bg-surface-100-800-token border-b border-color"
	>
		<Fa icon={getStatusIcon(document.status)} size="lg" class="dimmed" />
		{#if input.provider === IO.File}
			<p>{document.path.split(input.path.replace('\\', '/') + '/').at(-1)}</p>
		{:else}
			<p>{document.path}</p>
		{/if}
		<button class="ml-auto" on:click={modalStore.close}>
			<Fa icon={faClose} />
		</button>
	</div>

	<div class="bg-surface-50-900-token opacity-75">
		{#if document.error}
			<div class="p-4 grid justify-center text-center text-lg variant-soft-error font-bold gap-8">
				<h3 class="h3">The document has encountered an error</h3>
				<p>
					{document.error}
				</p>
			</div>
		{/if}
		<div
			class="p-4 grid grid-cols-2 md:grid-cols-4 gap-4 justify-center items-center border-b border-color text-sm"
		>
			<div class="flex flex-col items-start justify-center gap-2">
				<p class="font-bold">Status</p>
				<p>{document.status}</p>
			</div>
			<div class="flex flex-col items-start justify-center gap-2">
				<p class="font-bold">Size</p>
				<p>{document.size ? formatFileSize(document.size) : 'Unknown'}</p>
			</div>
			{#if URLIn}
				<a href={URLIn} target="_blank" class="flex flex-col items-start justify-center gap-2">
					<p class="anchor font-bold">Source</p>
					<p>{input.provider}</p>
				</a>
			{:else}
				<div class="flex flex-col items-start justify-center gap-2">
					<p class="font-bold">Source</p>
					<p>{input.provider}</p>
				</div>
			{/if}
			{#if URLOut}
				<a href={URLOut} target="_blank" class="flex flex-col items-start justify-center gap-2">
					<p class="anchor font-bold">Target</p>
					<p>{output.provider}</p>
				</a>
			{:else}
				<div class="flex flex-col items-start justify-center gap-2">
					<p class="font-bold">Target</p>
					<p>{output.provider}</p>
				</div>
			{/if}
		</div>
		<div
			class="p-4 grid-cols-2 grid md:grid-cols-4 gap-4 justify-center items-center border-b border-color text-sm"
		>
			<div class="flex flex-col items-start justify-center gap-2">
				<p class="font-bold">Wait</p>
				<p>{formatMilliseconds(document.duration_wait)}</p>
			</div>
			<div class="flex flex-col items-start justify-center gap-2">
				<p class="font-bold">Setup</p>
				<p>{formatMilliseconds(document.duration_decode + document.duration_deserialize)}</p>
			</div>

			<div class="flex flex-col items-start justify-center gap-2">
				<p class="font-bold">Process</p>
				<p>{formatMilliseconds(document.duration_process)}</p>
			</div>
			<div class="flex flex-col items-start justify-center gap-2">
				<p class="font-bold">Total</p>
				<p>{formatMilliseconds(document.duration || 0)}</p>
			</div>
		</div>
		<div class="p-4 flex flex-col gap-4 border-b border-color">
			<div class="flex items-end gap-4 justify-end">
				<button
					class="button-neutral mr-auto"
					on:click={() => (annotationsExpanded = !annotationsExpanded)}
				>
					<div class:turn={annotationsExpanded} class="transition-transform duration-300">
						<Fa icon={faChevronUp} size="lg" />
					</div>
					<span>{annotationsExpanded ? 'Collapse' : 'Expand'}</span>
				</button>
				<Number label="Minimum count" name="minimumCount" bind:value={minimumCount} />
				<Search placeholder="Search" bind:query={searchText} />
			</div>

			<div class:open={annotationsExpanded} class="content dimmed">
				<div class="content-wrapper space-y-4">
					{#if document.annotations && Object.entries(document.annotations).length > 0}
						<div
							class="self-stretch text-sm overflow-hidden grid sm:grid-cols-2 xl:grid-cols-3 gap-4"
						>
							{#each annotationFilter.entries() as [key, value]}
								<div class="grid grid-cols-2 input-wrapper gap-4 p-4">
									<div class="flex flex-col items-start justify-center gap-2">
										<p class="font-bold">Class</p>
										<p class="">{key.split('.').slice(-1)}</p>
									</div>
									<div class="flex flex-col items-start justify-center gap-2">
										<p class="font-bold">Count</p>
										<p>{value}</p>
									</div>
								</div>
							{/each}
						</div>
					{/if}
				</div>
			</div>
			{#if loaded}
				<div use:chart={options} />
				{#if document.events}
					<div use:chart={eventOptions} />
				{/if}
			{/if}
		</div>
	</div>
</div>

<style>
	.content-wrapper {
		overflow: hidden;
	}

	.content {
		display: grid;
		grid-template-rows: 0fr;
		transition: grid-template-rows 300ms;
	}

	.open {
		grid-template-rows: 1fr;
	}

	.turn {
		transform: rotate(-180deg);
	}
</style>
