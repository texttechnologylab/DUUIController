<!--	
	@component
	A component that displays metrics and other information about a DUUIDocument
-->
<script lang="ts">
	import { IO, type DUUIDocument, type DUUIDocumentProvider } from '$lib/duui/io'
	import { Status } from '$lib/duui/monitor'
	import type { DUUIPipeline } from '$lib/duui/pipeline'
	import type { DUUIProcess } from '$lib/duui/process'
	import { formatFileSize, includes } from '$lib/duui/utils/text'
	import { errorToast, getStatusIcon } from '$lib/duui/utils/ui'
	import { isDarkModeStore, userSession } from '$lib/store'
	import { faChevronDown, faClose, faDownload, faRefresh } from '@fortawesome/free-solid-svg-icons'
	import { getDrawerStore, getToastStore } from '@skeletonlabs/skeleton'
	import { onMount } from 'svelte'
	import Fa from 'svelte-fa'
	import {
		getAnnotationsPlotOptions,
		getTimelinePlotOptions
	} from '../../../../routes/processes/[oid]/chart'
	import Number from '../Input/Number.svelte'
	import Search from '../Input/Search.svelte'

	const drawerStore = getDrawerStore()

	let _document: DUUIDocument = $drawerStore.meta.document
	let process: DUUIProcess = $drawerStore.meta.process
	let pipeline: DUUIPipeline = $drawerStore.meta.pipeline

	const input: DUUIDocumentProvider = process.input
	const output: DUUIDocumentProvider = process.output

	let URLIn: string = ''
	let URLOut: string = ''

	const toastStore = getToastStore()

	let annotationsExpanded: boolean = true
	let searchText: string = ''
	let minimumCount: number = 1
	let annotationFilter: Map<string, number> = new Map(Object.entries(_document.annotations || {}))
	let downloading: boolean = false

	const download = async () => {
		downloading = true
		const response = await fetch(
			`/api/files/download?provider=${output.provider}&path=${output.path}/${_document.name.replace(
				input.file_extension,
				output.file_extension
			)}`,
			{
				method: 'GET'
			}
		)

		if (response.ok) {
			const blob = await response.blob()
			const url = URL.createObjectURL(blob)
			const anchor = document.createElement('a')
			anchor.href = url
			anchor.download = _document.name.replace(input.file_extension, output.file_extension)
			document.body.appendChild(anchor)
			anchor.click()
			document.body.removeChild(anchor)
			URL.revokeObjectURL(url)
		} else {
			toastStore.trigger(errorToast(await response.text()))
		}

		downloading = false
	}

	switch (input.provider) {
		case IO.Dropbox:
			URLIn = 'https://www.dropbox.com/home/Apps/Docker Unified UIMA Interface'
			break
		case IO.Minio:
			URLIn = $userSession?.connections.minio.endpoint || ''
			break
		default:
			URLIn = ''
	}

	switch (output.provider) {
		case IO.Dropbox:
			URLOut = 'https://www.dropbox.com/home/Apps/Docker Unified UIMA Interface'
			break
		case IO.Minio:
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

	let options
	let eventOptions

	$: {
		options = getAnnotationsPlotOptions(annotationFilter, $isDarkModeStore)
		eventOptions = getTimelinePlotOptions(process, pipeline, _document, $isDarkModeStore)
	}

	onMount(() => {
		async function loadApexCharts() {
			const module = await import('apexcharts')
			ApexCharts = module.default
			window.ApexCharts = ApexCharts
			loaded = true
		}

		loadApexCharts()
	})

	$: {
		if (_document.annotations) {
			annotationFilter = new Map(
				Object.entries(_document.annotations).filter(
					(entry) =>
						(includes(entry[0], searchText) || searchText === '') && entry[1] >= minimumCount
				)
			)
		}
	}
</script>

<div class="h-full bg-surface-50-900-token">
	<div id="scroll-top" />
	<div
		class="w-full z-50 grid
		font-bold text-2xl p-4 border-surface-200 dark:border-surface-500 sm:flex items-center justify-start gap-4 sticky top-0 bg-surface-100-800-token border-b border-color"
	>
		<div class="flex-center-4">
			<Fa icon={getStatusIcon(_document.status)} size="lg" class="dimmed" />
			{#if input.provider === IO.File}
				<p class="text-sm md:text-base max-w-[10ch]">
					{_document.name}
				</p>
			{:else}
				<p class="text-sm md:text-base">{_document.path}</p>
			{/if}
		</div>
		<div class="ml-auto justify-start items-center gap-4 flex">
			{#if output.provider !== IO.None && _document.status === Status.Completed}
				{#if downloading}
					<button class="button-neutral opacity-50">
						<Fa icon={faRefresh} spin />
						<span>Loading</span>
					</button>
				{:else}
					<button class="button-neutral" on:click={download}>
						<Fa icon={faDownload} />
						<span>Download</span>
					</button>
				{/if}
			{/if}
			<button class="button-neutral" on:click={drawerStore.close}>
				<Fa icon={faClose} />
				<span>Close</span>
			</button>
		</div>
	</div>

	<div class="bg-surface-50-900-token">
		{#if _document.error}
			<div class="p-4 grid justify-center text-center text-lg variant-soft-error font-bold gap-8">
				<h3 class="h3">The document has encountered an error</h3>
				<p class="mx-auto">
					{_document.error}
				</p>
			</div>
		{/if}
		<div
			class="p-4 grid grid-cols-2 md:grid-cols-4 gap-4 justify-center items-center border-b border-color text-sm md:text-base"
		>
			<div class="flex flex-col items-start justify-center gap-2">
				<p class="font-bold">Status</p>
				<p>{_document.status}</p>
			</div>
			<div class="flex flex-col items-start justify-center gap-2">
				<p class="font-bold">Size</p>
				<p>{_document.size ? formatFileSize(_document.size) : 'Unknown'}</p>
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
			{#if output.provider !== IO.None && _document.status === Status.Completed}
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
		<!-- <div
			class="p-4 grid-cols-2 grid md:grid-cols-4 gap-4 justify-center items-center border-b border-color text-sm md:text-base"
		>
			<div class="flex flex-col items-start justify-center gap-2">
				<p class="font-bold">Setup</p>
				<p>{formatMilliseconds(_document.duration_wait)}</p>
			</div>
			<div class="flex flex-col items-start justify-center gap-2">
				<p class="font-bold">Wait</p>
				<p>{formatMilliseconds(_document.duration_wait)}</p>
			</div>

			<div class="flex flex-col items-start justify-center gap-2">
				<p class="font-bold">Process</p>
				<p>{formatMilliseconds(_document.duration_process)}</p>
			</div>
			<div class="flex flex-col items-start justify-center gap-2">
				<p class="font-bold">Total</p>
				<p>{formatMilliseconds(_document.duration || 0)}</p>
			</div>
		</div> -->
		<div class="p-4 flex flex-col gap-4 border-b border-color">
			{#if _document.annotations && Object.entries(_document.annotations).length > 0}
				<h2 class="h2">Annotations</h2>
				<div class="flex items-end gap-4 justify-end">
					<button
						class="button-neutral mr-auto"
						on:click={() => (annotationsExpanded = !annotationsExpanded)}
					>
						<div class:turn={annotationsExpanded} class="transition-transform duration-300">
							<Fa icon={faChevronDown} size="lg" />
						</div>
						<span>{annotationsExpanded ? 'Collapse' : 'Expand'}</span>
					</button>
					<div class="grid md:grid-cols-2 items-end gap-4">
						<Number label="Minimum count" name="minimumCount" bind:value={minimumCount} />
						<Search placeholder="Search" bind:query={searchText} />
					</div>
				</div>

				<div class:open={annotationsExpanded} class="content dimmed">
					<div class="content-wrapper space-y-4">
						<div
							class="self-stretch text-sm overflow-hidden grid sm:grid-cols-2 xl:grid-cols-3 gap-4"
						>
							{#each annotationFilter.entries() as [key, value]}
								<div class="grid grid-cols-2 card gap-4 p-4">
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
					</div>
				</div>
				{#if loaded}
					<div use:chart={options} />
				{/if}
			{/if}
			{#if loaded && _document.events}
				<h2 class="h2">Timeline</h2>
				<div class="pr-4" use:chart={eventOptions} />
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
