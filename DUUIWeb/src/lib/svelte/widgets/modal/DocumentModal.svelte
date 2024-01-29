<script lang="ts">
	import { type DUUIDocument, type DUUIDocumentProvider } from '$lib/duui/io'
	import type { DUUIProcess } from '$lib/duui/process'
	import { formatFileSize } from '$lib/duui/utils/text'
	import { formatMilliseconds } from '$lib/duui/utils/time'
	import { getStatusIcon, scrollIntoView } from '$lib/duui/utils/ui'
	import { faCheckDouble, faWarning } from '@fortawesome/free-solid-svg-icons'
	import { getModalStore } from '@skeletonlabs/skeleton'
	import { onMount } from 'svelte'
	import Fa from 'svelte-fa'
	import Timeline from './Timeline.svelte'
	import { DropboxAppURL } from '$lib/config'
	import { userSession } from '$lib/store'

	export let input: DUUIDocumentProvider
	export let output: DUUIDocumentProvider
	const modalStore = getModalStore()

	let document: DUUIDocument = $modalStore[0].meta.document
	let process: DUUIProcess = $modalStore[0].meta.process
	let URLIn: string = ''
	let URLOut: string = ''

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
				document.name.replace('.' + document.name.split('.').at(-1), output.fileExtension)
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
	let annotations: Map<string, number> = new Map()

	if (document.annotations) {
		for (let annotation of Object.entries(document.annotations)) {
			const name: string | undefined = annotation[0].split('.').at(-1)
			if (!name) continue

			const value: number | undefined = annotation[1]
			if (!value) continue

			if (annotations.has(name)) {
				annotations.set(name, annotations.get(name) || 0 + value)
			} else {
				annotations.set(name, value)
			}
		}
	}

	let options = {
		series: Array.from(annotations.values()),
		chart: {
			type: 'donut',
			height: '500px'
		},
		title: {
			text: 'Annotations',
			align: 'center',
			offsetX: 0,
			offsetY: 0,
			floating: false,
			style: {
				fontSize: '32px',
				fontWeight: 'bold'
			}
		},
		dataLabels: {
			dropShadow: {
				blur: 3,
				opacity: 0.8
			}
		},
		labels: Array.from(annotations.keys()),
		theme: {
			monochrome: {
				enabled: true,
				color: '#006c98'
			}
		},
		legend: {
			show: false,
			position: 'bottom',
			horizontalAlign: 'left'
		},
		plotOptions: {
			pie: {
				donut: {
					labels: {
						show: true,
						total: {
							show: true,
							formatter: (w) => {
								return w.globals.seriesTotals.reduce((a, b) => {
									return a + b
								})
							}
						}
					}
				}
			}
		}
	}

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
</script>

<div class="section-wrapper container max-w-6xl sticky top-0 max-h-[90vh] !overflow-y-auto">
	<div id="scroll-top" />
	<div
		class="font-bold text-2xl p-4 border-surface-200 dark:border-surface-500 text-center flex items-center justify-between sticky top-0 z-10 bg-surface-50-900-token"
	>
		<p>{document.name}</p>
		{#if document.error}
			<Fa icon={faWarning} size="lg" class="text-error-500" />
		{:else if document.is_finished}
			<Fa icon={faCheckDouble} size="lg" class="text-success-500" />
		{:else}
			<Fa icon={getStatusIcon(document.status)} size="lg" />
		{/if}
	</div>

	<div class="p-4 space-y-8 bg-surface-100-800-token opacity-75">
		{#if document.error}
			<p class="section-wrapper text-lg text-error-500 font-bold p-4">
				{document.error}
			</p>
		{/if}
		<div
			class="grid grid-cols-2 md:grid-cols-4 justify-center items-center section-wrapper text-sm"
		>
			<div
				class="bg-fancy flex flex-col items-start justify-center gap-2 border-r border-surface-200 dark:border-surface-500 p-4"
			>
				<p class="font-bold">Status</p>
				<p>{document.status}</p>
			</div>
			{#if URLIn}
				<a
					href={URLIn}
					target="_blank"
					class="bg-fancy flex flex-col items-start justify-center gap-2 border-r border-surface-200 dark:border-surface-500 p-4"
				>
					<p class="anchor font-bold">Source</p>
					<p>{input.provider}</p>
				</a>
			{:else}
				<div
					class="bg-fancy flex flex-col items-start justify-center gap-2 border-r border-surface-200 dark:border-surface-500 p-4"
				>
					<p class="font-bold">Source</p>
					<p>{input.provider}</p>
				</div>
			{/if}
			{#if URLOut}
				<a
					href={URLOut}
					target="_blank"
					class="bg-fancy flex flex-col items-start justify-center gap-2 border-r border-surface-200 dark:border-surface-500 p-4"
				>
					<p class="anchor font-bold">Target</p>
					<p>{output.provider}</p>
				</a>
			{:else}
				<div
					class="bg-fancy flex flex-col items-start justify-center gap-2 border-r border-surface-200 dark:border-surface-500 p-4"
				>
					<p class="font-bold">Target</p>
					<p>{output.provider}</p>
				</div>
			{/if}
			<div class="bg-fancy flex flex-col items-start justify-center gap-2 p-4">
				<p class="font-bold">Size</p>
				<p>{document.size ? formatFileSize(document.size) : 'Unknown'}</p>
			</div>
		</div>
		<div class="grid grid-cols-3 section-wrapper rounded-md text-sm">
			<div
				class="bg-fancy flex flex-col items-start justify-center gap-2 border-r border-surface-200 dark:border-surface-500 p-4"
			>
				<p class="font-bold">Wait</p>
				<p>{formatMilliseconds(document.duration_wait)}</p>
			</div>
			<div
				class="bg-fancy flex flex-col items-start justify-center gap-2 border-r border-surface-200 dark:border-surface-500 p-4"
			>
				<p class="font-bold">Setup</p>
				<p>{formatMilliseconds(document.duration_decode + document.duration_deserialize)}</p>
			</div>

			<div class="bg-fancy flex flex-col items-start justify-center gap-2 p-4">
				<p class="font-bold">Process</p>
				<p>{formatMilliseconds(document.duration_process)}</p>
			</div>
			<div
				class="bg-fancy col-span-3 border-t border-surface-200 dark:border-surface-500 p-4 text-center font-bold text-lg"
			>
				<p class="mx-auto">
					Total {formatMilliseconds(document.duration || 0)}
				</p>
			</div>
		</div>
		{#if document.annotations}
			{#if loaded}
				<div class="section-wrapper p-4 text-center">
					<div use:chart={options} />
				</div>
			{/if}
			<div class="section-wrapper text-sm">
				{#each Object.entries(document.annotations) as entry}
					<div class="grid sm:grid-cols-2 border-b border-surface-200 dark:border-surface-500">
						<div class="bg-fancy flex md:flex-col items-start justify-start gap-2 p-4">
							<p class="font-bold">Class</p>
							<p class="">{entry[0].split('.').slice(-1)}</p>
						</div>
						<div class="bg-fancy flex md:flex-col items-start justify-start gap-2 p-4">
							<p class="font-bold">Count</p>
							<p>{entry[1]}</p>
						</div>
					</div>
				{/each}
			</div>
			{#if document.events}
				<Timeline {process} {document} />
			{/if}
			<!-- <div class="section-wrapper text-sm">
				{#each Object.entries(document.annotations) as entry}
					<div class="grid md:grid-cols-2 border-b border-surface-200 dark:border-surface-500">
						<div class="bg-fancy flex md:flex-col items-start justify-center gap-2 p-4">
							<p class="font-bold">Class</p>
							<p class="">{entry[0].split('.').slice(-1)}</p>
						</div>
						<div class="bg-fancy flex md:flex-col items-start justify-center gap-2 p-4">
							<p class="font-bold">Count</p>
							<p>{entry[1]}</p>
						</div>
						<div
							class="bg-fancy md:flex flex-col items-start justify-center gap-2 p-4 col-span-2 hidden"
						>
							<p class="font-bold">Annotation</p>
							<p class="">{entry[0]}</p>
						</div>
					</div>
				{/each}
			</div> -->
		{/if}
	</div>
</div>
