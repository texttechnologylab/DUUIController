<script lang="ts">
	import { goto } from '$app/navigation'
	import {
		Input,
		Output,
		getTotalDuration,
		isCloudProvider,
		type DUUIDocument
	} from '$lib/duui/io.js'
	import { Status, isActive } from '$lib/duui/monitor.js'
	import { processToSeachParams } from '$lib/duui/process.js'
	import { Api, makeApiCall } from '$lib/duui/utils/api'
	import { equals, formatFileSize, progresAsPercent, snakeToTitleCase } from '$lib/duui/utils/text'
	import { formatMilliseconds, getDuration } from '$lib/duui/utils/time'
	import {
		documentStatusNames,
		getDocumentStatusIcon,
		getStatusIcon,
		infoToast,
		successToast
	} from '$lib/duui/utils/ui'
	import DocumentModal from '../../../lib/svelte/widgets/modal/DocumentModal.svelte'

	import {
		faArrowDownWideShort,
		faArrowLeft,
		faArrowUpWideShort,
		faCancel,
		faRefresh,
		faSearch,
		faTrash
	} from '@fortawesome/free-solid-svg-icons'
	import {
		ProgressBar,
		getModalStore,
		getToastStore,
		type ModalComponent,
		type ModalSettings
	} from '@skeletonlabs/skeleton'

	import Search from '$lib/svelte/widgets/input/Search.svelte'
	import Select from '$lib/svelte/widgets/input/Select.svelte'
	import Paginator from '$lib/svelte/widgets/navigation/Paginator.svelte'
	import { onMount } from 'svelte'
	import Fa from 'svelte-fa'

	export let data
	const toastStore = getToastStore()

	let { pipeline, process, documentQuery, timeline } = data

	let documents: DUUIDocument[] = documentQuery.documents

	let progressPercent: number = 0

	let tableHeader: string[] = ['Name', 'Progress', 'Status', 'File Size', 'Duration']

	let paginationSettings: PaginationSettings = {
		page: 0,
		limit: 10,
		total: documentQuery.count,
		sizes: [5, 10, 20, 50]
	}

	let sort: Sort = {
		by: 0,
		order: 1
	}

	const sortMap: Map<number, string> = new Map([
		[0, 'name'],
		[1, 'progress'],
		[2, 'status'],
		[3, 'size'],
		[4, 'duration']
	])

	let searchText: string = ''

	let filter: string[] = [Status.Any]
	let maxProgress = pipeline.components.length

	let ApexCharts
	let loaded: boolean = false

	let annotations: Map<string, number> = new Map()
	let statusMap: Map<string, number> = new Map()

	// let options = {
	// 	series: Array.from(statusMap.values()),
	// 	chart: {
	// 		type: 'donut'
	// 	},
	// 	labels: Array.from(statusMap.keys()),
	// 	responsive: [
	// 		{
	// 			breakpoint: 480,
	// 			options: {
	// 				chart: {
	// 					width: 200
	// 				},
	// 				legend: {
	// 					position: 'bottom'
	// 				}
	// 			}
	// 		}
	// 	]
	// }

	// let timeChart = {
	// 	series: [
	// 		{
	// 			data: documents.map((d) => {
	// 				return {
	// 					x: d.name,
	// 					y: [
	// 						((d.startTime > 0 ? d.startTime : process.startTime) - process.startTime) / 1000,
	// 						((d.endTime > 0 ? d.endTime : new Date().getTime() + 500) - process.startTime) / 1000
	// 					]
	// 				}
	// 			})
	// 		}
	// 	],
	// 	chart: {
	// 		height: 600,
	// 		type: 'rangeBar',
	// 		zoom: {
	// 			enabled: false
	// 		}
	// 	},
	// 	colors: ['#006c98', '#36BDCB'],
	// 	plotOptions: {
	// 		bar: {
	// 			horizontal: true,
	// 			borderRadius: 4
	// 		}
	// 	},
	// 	grid: {
	// 		xaxis: {
	// 			lines: {
	// 				show: true
	// 			}
	// 		},
	// 		yaxis: {
	// 			lines: {
	// 				show: true
	// 			}
	// 		}
	// 	},
	// 	labels: {
	// 		show: true,
	// 		formatter: function (val) {
	// 			return formatMilliseconds(val * 1000)
	// 		}
	// 	}
	// }

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

	onMount(() => {
		// async function loadApexCharts() {
		// 	const module = await import('apexcharts')
		// 	ApexCharts = module.default
		// 	window.ApexCharts = ApexCharts
		// 	loaded = true
		// }

		async function updateProcess() {
			const response = await fetch(
				`./api/update/${process.oid}
				?limit=${paginationSettings.limit}
				&skip=${paginationSettings.limit * paginationSettings.page}
				&sort=${sortMap.get(sort.by)}
				&order=${sort.order}
				&text=${searchText}
				&filter=${filter.join(';')}`,
				{
					method: 'GET'
				}
			)

			if (!response.ok || !isActive(process.status)) {
				clearInterval(interval)
			}

			const json = await response.json()

			process = json.process
			timeline = json.timeline

			// for (let document of documents) {
			// 	for (let annotation of Object.entries(document.annotations)) {
			// 		const name: string | undefined = annotation[0].split('.').at(-1)
			// 		if (!name) continue

			// 		const value: number | undefined = annotation[1]
			// 		if (!value) continue

			// 		if (annotations.has(name)) {
			// 			annotations.set(name, annotations.get(name) || 0 + value)
			// 		} else {
			// 			annotations.set(name, value)
			// 		}
			// 	}
			// }

			progressPercent = progresAsPercent(process.progress, process.document_names.length)
			updateTable()

			if (progressPercent > 100) progressPercent = 100
			if (process.is_finished) {
				clearInterval(interval)
			}
		}

		const interval = setInterval(updateProcess, 1000)
		updateProcess()
		// loadApexCharts()
		return () => clearInterval(interval)
	})

	const cancelProcess = async () => {
		const response = await makeApiCall(Api.Processes, 'PUT', { oid: process.oid })
		process.status = Status.Cancelled
		process.is_finished = true

		if (response.ok) {
			toastStore.trigger(successToast('Process has been cancelled'))
		} else {
			toastStore.trigger(infoToast('Process has already been cancelled'))
		}
	}

	const deleteProcess = async () => {
		new Promise<boolean>((resolve) => {
			const modal: ModalSettings = {
				type: 'component',
				component: 'deleteModal',
				meta: {
					title: 'Delete process',
					body: 'Are you sure you want to delete this process?'
				},
				response: (r: boolean) => {
					resolve(r)
				}
			}
			modalStore.trigger(modal)
		}).then(async (accepted: boolean) => {
			if (!accepted) return

			const response = await makeApiCall(Api.Processes, 'DELETE', { oid: process.oid })
			if (response.ok) {
				toastStore.trigger(infoToast('Process has been deleted'))
				goto(`/pipelines/${pipeline.oid}`)
			}
		})
	}

	const DROPBOX_URL: string = 'https://www.dropbox.com/home/Apps/Cedric%20Test%20App'

	function getOutput(): string {
		if (equals(process.output.provider, Output.Minio)) {
			return `http://192.168.2.122:9090/browser/${process.output.path}`
		}

		if (equals(process.output.provider, Output.Dropbox)) {
			return `${DROPBOX_URL}${process.output.path}`
		}

		return ''
	}

	function getInput(): string {
		if (equals(process.input.provider, Input.Minio)) {
			return `http://192.168.2.122:9090/browser/${process.input.path}`
		}

		if (equals(process.input.provider, Input.Dropbox)) {
			return `${DROPBOX_URL}${process.input.path}`
		}

		return ''
	}

	async function restart() {
		goto(
			`/process?pipeline_id=${pipeline.oid}
			&from=/process/${process.oid}
			&${processToSeachParams(process)}`
		)
	}

	const updateTable = async () => {
		const lastFilter: string | undefined = filter.at(-1)

		if (equals(lastFilter || '', Status.Any) || filter.length === 0) {
			filter = [Status.Any]
		} else {
			filter = filter.filter((status) => !equals(status, Status.Any))
		}

		const response = await fetch(
			`./api/documents
			?process_id=${process.oid}
			&limit=${paginationSettings.limit}
			&skip=${paginationSettings.page * paginationSettings.limit}
			&sort=${sortMap.get(sort.by)}
			&text=${searchText}
			&order=${sort.order}
			&filter=${filter.join(';')}`,
			{
				method: 'GET'
			}
		)

		const json = await response.json()
		const data = json.result

		documents = data.documents
		paginationSettings.total = data.count
	}

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

	const sortTable = (index: number) => {
		sort.order = sort.by !== index ? 1 : sort.order === 1 ? -1 : 1
		sort.by = index
		updateTable()
	}
</script>

<div class="">
	<div class="grid">
		<div
			class="page-wrapper bg-solid md:top-0 z-10 left-0 bottom-0 right-0 row-start-2 fixed md:sticky md:row-start-1"
		>
			<div class="grid grid-cols-3 md:flex items-center md:justify-start gap-4 relative">
				<a class="button-primary" href={`/pipelines/${pipeline.oid}`}>
					<Fa icon={faArrowLeft} />
					<span class="hidden md:inline">Back</span>
				</a>
				{#if process.is_finished}
					<button class="button-primary" on:click={restart}>
						<Fa icon={faRefresh} />
						<span class="hidden md:inline">Restart</span>
					</button>
					<button class="button-error md:ml-auto" on:click={deleteProcess}>
						<Fa icon={faTrash} />
						<span class="hidden md:inline">Delete</span>
					</button>
				{:else}
					<button class="button-error md:ml-auto" on:click={cancelProcess}>
						<Fa icon={faCancel} />
						<span class="hidden md:inline">Cancel</span>
					</button>
				{/if}
			</div>
		</div>
		<div class="p-4 md:p-8 space-y-4">
			<div class="flex items-center justify-between gap-4 mb-8">
				<div class="space-y-2">
					<h1 class="h1">{pipeline.name}</h1>
					<div>
						<p>Total process duration: {getDuration(process.started_at, process.finished_at)}</p>
						<p>
							{process.progress} of {process.document_names.length} Documents have been processed.
						</p>
					</div>
				</div>

				<div class="flex items-center gap-4">
					<Fa
						icon={getStatusIcon(process.status)}
						size="2x"
						class={equals(process.status, Status.Active) ? 'animate-spin-slow ' : ''}
					/>
					<p class="text-lg hidden md:block">{process.status}</p>
				</div>
			</div>
			<div>
				<div class=" md:text-base flex items-end">
					{#if process.error}
						<p class="text-error-500 font-bold p-2 md:max-w-[60ch] max-w-[40ch]">
							ERROR: {process.error}
						</p>
					{/if}
					<div
						class="hidden ml-auto md:flex overflow-hidden justify-between section-wrapper !shadow-none !border-b-0 !rounded-b-none z-10"
					>
						<Search
							style="bg-fancy py-3"
							bind:query={searchText}
							placeholder="Search..."
							icon={faSearch}
							on:focusout={() => updateTable()}
							on:keydown={(event) => {
								if (equals(event.key, 'enter')) {
									updateTable()
								}
							}}
						/>
						<Select
							style="z-50 !rounded-none hidden md:flex"
							border="border-l border-color"
							label="Status"
							name="Status"
							on:change={updateTable}
							bind:selected={filter}
							options={documentStatusNames}
						/>
					</div>
				</div>

				<div class="section-wrapper">
					<div
						class="header grid grid-cols-3 md:grid-cols-4 lg:grid-cols-5 bg-surface-100 dark:variant-soft-surface"
					>
						{#each tableHeader as column, index}
							<button
								class="btn-sm md:text-base md:inline-flex bg-surface-100 px-4 dark:variant-soft-surface gap-4 justify-start items-center rounded-none p-3 text-left
							{[4].includes(index)
									? 'hidden md:inline-flex'
									: [3].includes(index)
									? 'hidden lg:inline-flex'
									: ''}"
								on:click={() => sortTable(index)}
							>
								<span>{column}</span>
								{#if sort.by === index}
									<Fa icon={sort.order === -1 ? faArrowDownWideShort : faArrowUpWideShort} />
								{/if}
							</button>
						{/each}
					</div>
					<ProgressBar
						value={process.progress}
						max={process.document_names.length}
						height="h-1"
						rounded="rounded-none"
						meter="variant-filled-primary"
					/>
					<div class="overflow-hidden flex flex-col">
						{#each documents as document}
							<button
								class="btn-sm text-xs md:text-sm rounded-none
							first:border-t-0 border-t-[1px]
							dark:border-t-surface-500 dark:hover:variant-soft-primary
							hover:variant-filled-primary
							grid grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-8 p-3 px-4 text-left"
								on:click={() => showDocumentModal(document)}
							>
								<p class="break-words">{document.name}</p>
								<div class="md:flex items-center justify-start md:gap-4">
									<p>
										{Math.round((Math.min(document.progress, maxProgress) / maxProgress) * 100)} %
									</p>
									<p>{Math.min(document.progress, maxProgress)} / {maxProgress}</p>
								</div>
								<p class="flex justify-start items-center gap-2 md:gap-4">
									<Fa
										icon={getDocumentStatusIcon(document)}
										size="lg"
										class="{equals(document.status, Status.Active)
											? 'animate-spin-slow'
											: equals(document.status, Status.Waiting)
											? 'animate-hourglass'
											: ''} w-6"
									/>
									<span>{document.status}</span>
								</p>
								<p class="hidden lg:block">{formatFileSize(document.size)}</p>
								<p class="hidden md:block">{formatMilliseconds(getTotalDuration(document))}</p>
							</button>
						{/each}
					</div>
				</div>
				<div class="py-4">
					<Paginator bind:settings={paginationSettings} on:change={updateTable} />
				</div>
			</div>

			<!-- <Timeline {process} {documents} /> -->

			<div class="section-wrapper space-y-8 p-8">
				<div class="grid md:grid-cols-2 gap-4">
					<div class="space-y-8">
						<h2 class="h2">Settings</h2>
						<div class="grid grid-cols-2 gap-4">
							{#each Object.entries(process.settings) as [key, value]}
								<p>{snakeToTitleCase(key)}</p>
								<p>
									{key === 'minimum_size'
										? formatFileSize(+value)
										: value === true
										? 'Yes'
										: value === false
										? 'No'
										: value}
								</p>
							{/each}
							{#if process.initial}
								<p>Documents found</p>
								<p>{process.initial}</p>
								<p>Documents skipped</p>
								<p>{process.skipped}</p>
								<p>Documents kept</p>
								<p>{process.initial - process.skipped}</p>
							{/if}
						</div>
					</div>

					{#if process.pipeline_status}
						<div class="space-y-8">
							<h2 class="h2">Pipeline Status</h2>
							<div class="grid grid-cols-2 gap-4">
								{#each Object.entries(process.pipeline_status) as [key, status]}
									<p>{key}</p>
									<p>{status}</p>
								{/each}
							</div>
						</div>
					{/if}
				</div>

				{#if loaded}
					<!-- <div>
						<h2 class="h2">Timeline</h2>

						<div use:chart={timeChart} />
					</div>
					<div class="max-w-[500px]">
						<h2 class="h2">Status</h2>

						<div use:chart={options} />
					</div> -->
				{/if}
			</div>
		</div>
	</div>
</div>
