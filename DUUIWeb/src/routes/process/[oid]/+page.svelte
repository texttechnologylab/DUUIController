<script lang="ts">
	import { goto } from '$app/navigation'
	import ActionButton from '$lib/svelte/widgets/action/ActionButton.svelte'
	import IconButton from '$lib/svelte/widgets/action/IconButton.svelte'
	import {
		Output,
		type DUUIDocument,
		isCloudProvider,
		getTotalDuration,
		Input
	} from '$lib/duui/io.js'
	import { Status, isActive } from '$lib/duui/monitor.js'
	import { processToSeachParams } from '$lib/duui/process.js'
	import { Api, makeApiCall } from '$lib/duui/utils/api'
	import { equals, formatFileSize, progresAsPercent } from '$lib/duui/utils/text'
	import { formatMilliseconds, getDuration } from '$lib/duui/utils/time'
	import {
		documentStatusNames,
		getDocumentStatusIcon,
		getStatusIcon,
		info,
		success
	} from '$lib/duui/utils/ui'
	import DocumentModal from '../../../lib/svelte/widgets/modal/DocumentModal.svelte'

	import {
		faArrowDownWideShort,
		faArrowLeft,
		faArrowUpWideShort,
		faCancel,
		faFileDownload,
		faFileUpload,
		faRefresh,
		faSearch,
		faTrash
	} from '@fortawesome/free-solid-svg-icons'
	import {
		getModalStore,
		getToastStore,
		ProgressBar,
		type ModalComponent,
		type ModalSettings
	} from '@skeletonlabs/skeleton'

	import { onMount } from 'svelte'
	import Fa from 'svelte-fa'
	import Select from '$lib/svelte/widgets/input/Select.svelte'
	import Search from '$lib/svelte/widgets/input/Search.svelte'
	import Timeline from '$lib/svelte/widgets/timeline/Timeline.svelte'
	import SpeedDial from '$lib/svelte/widgets/navigation/SpeedDial.svelte'
	import Paginator from '$lib/svelte/widgets/navigation/Paginator.svelte'
	// import { chart } from 'svelte-apexcharts'

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
	let maxProgress = pipeline.components.length + (isCloudProvider(process.output.target) ? 1 : 0)

	onMount(() => {
		async function updateProcess() {
			const response = await fetch(
				`./api/update/${process.oid}
				?limit=${paginationSettings.limit}
				&skip=${paginationSettings.limit * paginationSettings.page}
				&by=${sortMap.get(sort.by)}
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

			progressPercent = progresAsPercent(process.progress, process.documentNames.length)
			updateTable()

			if (progressPercent > 100) progressPercent = 100
			if (process.finished) {
				clearInterval(interval)
			}
		}

		const interval = setInterval(updateProcess, 1000)
		updateProcess()
		return () => clearInterval(interval)
	})

	const cancelProcess = async () => {
		const response = await makeApiCall(Api.Processes, 'PUT', { oid: process.oid })
		if (response.ok) {
			toastStore.trigger(success('Process has been canceled'))
			process.status = Status.Canceled
			process.finished = true
		} else {
			toastStore.trigger(info('Process has already been canceled'))
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
				toastStore.trigger(info('Process has been deleted'))
				goto(`/pipelines/${pipeline.oid}?tab=2`)
			}
		})
	}

	const DROPBOX_URL: string = 'https://www.dropbox.com/home/Apps/Cedric%20Test%20App'

	function getOutput(): string {
		if (equals(process.output.target, Output.Minio)) {
			return `http://192.168.2.122:9090/browser/${process.output.folder}`
		}

		if (equals(process.output.target, Output.Dropbox)) {
			return `${DROPBOX_URL}${process.output.folder}`
		}

		return ''
	}

	function getInput(): string {
		if (equals(process.input.source, Input.Minio)) {
			return `http://192.168.2.122:9090/browser/${process.input.folder}`
		}

		if (equals(process.input.source, Input.Dropbox)) {
			return `${DROPBOX_URL}${process.input.folder}`
		}

		return ''
	}

	async function restart() {
		goto(
			`/process?oid=${pipeline.oid}
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
			&by=${sortMap.get(sort.by)}
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

<SpeedDial>
	<svelte:fragment slot="content">
		<IconButton icon={faArrowLeft} on:click={() => goto('/pipelines/' + pipeline.oid + '?tab=2')} />
		{#if isCloudProvider(process.input.source)}
			<IconButton icon={faFileDownload} on:click={() => window.open(getInput())} />
		{/if}
		{#if isCloudProvider(process.output.target)}
			<IconButton icon={faFileUpload} on:click={() => window.open(getOutput())} />
		{/if}

		{#if !process.finished}
			<IconButton icon={faCancel} on:click={cancelProcess} />
		{:else}
			<IconButton icon={faRefresh} on:click={restart} />
			<IconButton icon={faTrash} on:click={deleteProcess} />
		{/if}
	</svelte:fragment>
</SpeedDial>

<div class="container h-full flex-col mx-auto flex gap-4 md:my-8 pb-32 space">
	<div class="flex items-end justify-between gap-4">
		<h1 class="h2">{pipeline.name}</h1>

		<div class="flex items-center gap-2 ml-auto">
			<Fa
				icon={getStatusIcon(process.status)}
				size="lg"
				class={equals(process.status, Status.Running) ? 'animate-spin-slow ' : ''}
			/>
			<p>{process.status}</p>
		</div>
	</div>

	<hr class="bg-surface-400/20 h-[1px] !border-0 rounded" />
	<div class="space-y-4">
		<div class="grid xl:flex items-center xl:justify-start gap-4">
			<div class="hidden lg:flex md:grid md:grid-cols-3 gap-4 items-center">
				<ActionButton
					icon={faArrowLeft}
					text="Back"
					leftToRight={true}
					on:click={() => goto('/pipelines/' + pipeline.oid + '?tab=2')}
				/>
				{#if isCloudProvider(process.input.source)}
					<ActionButton
						text="Input"
						icon={faFileDownload}
						on:click={() => window.open(getInput())}
					/>
				{/if}
				{#if isCloudProvider(process.output.target) && equals(process.status, Status.Completed)}
					<ActionButton
						text="Output"
						icon={faFileUpload}
						on:click={() => window.open(getOutput())}
					/>
				{/if}
				{#if !process.finished}
					<ActionButton text="Cancel" icon={faCancel} on:click={cancelProcess} />
				{:else}
					<ActionButton text="Restart" icon={faRefresh} on:click={restart} />
					<ActionButton text="Delete" icon={faTrash} on:click={deleteProcess} />
				{/if}
			</div>

			<div class="grid md:flex items-center gap-4 lg:ml-auto">
				<Search
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
					label="Status"
					name="Status"
					on:change={updateTable}
					bind:selected={filter}
					options={documentStatusNames}
				/>
			</div>
		</div>
		{#if process.error}
			<p class="text-error-400 font-bold break-words">ERROR: {process.error}</p>
		{/if}
		<div
			class="rounded-md overflow-hidden border border-surface-200 dark:border-surface-500 shadow-lg"
		>
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
				max={process.documentNames.length}
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
							<p>{Math.round((Math.min(document.progress, maxProgress) / maxProgress) * 100)} %</p>
							<p>{Math.min(document.progress, maxProgress)} / {maxProgress}</p>
						</div>
						<p class="flex justify-start items-center gap-2 md:gap-4">
							<Fa
								icon={getDocumentStatusIcon(document)}
								size="lg"
								class="{equals(document.status, Status.Running)
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

		<Paginator bind:settings={paginationSettings} on:change={updateTable} />
	</div>

	<Timeline {process} {documents} />

	<div
		class="rounded-md border-[1px] border-surface-200 dark:border-surface-500 shadow-lg grid items-start p-8 space-y-4"
	>
		<h2 class="h2">Statistics</h2>
		<pre>{JSON.stringify(process, null, 2)}</pre>
		<!-- <div class="">
			<Label name="Number of documents" value={'' + process.documentNames.length} />
			<Label
				name="Instantiation Duration"
				value={formatMilliseconds(process.instantiationDuration)}
			/>
			<Label name="Process Duration" value={getDuration(process.startTime, process.endTime)} />
			<Label
				name="Average document process duration"
				value={formatMilliseconds(
					documents
						.filter((d) => d.finished)
						.map((d) => d.processDuration)
						.reduce((total, num) => total + num, 0) / documents.filter((d) => d.finished).length
				)}
			/>
			<Label
				name="Slowest document process duration"
				value={documents.filter((d) => d.finished).length === 0
					? '-'
					: formatMilliseconds(
							Math.max(...documents.filter((d) => d.finished).map((d) => d.processDuration)) || 0
					  )}
			/>
			<Label
				name="Fastest document process duration"
				value={documents.filter((d) => d.finished).length === 0
					? '-'
					: formatMilliseconds(
							Math.min(...documents.filter((d) => d.finished).map((d) => d.processDuration))
					  )}
			/>
		</div> -->
	</div>
</div>
