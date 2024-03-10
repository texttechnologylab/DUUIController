<script lang="ts">
	import { goto } from '$app/navigation'
	import { IO, getTotalDuration, type DUUIDocument } from '$lib/duui/io.js'
	import { Status, isActive } from '$lib/duui/monitor.js'
	import { processToSeachParams } from '$lib/duui/process.js'
	import { equals, formatFileSize, progresAsPercent, snakeToTitleCase } from '$lib/duui/utils/text'
	import { formatMilliseconds, getDuration } from '$lib/duui/utils/time'
	import {
		documentStatusNames,
		getDocumentStatusIcon,
		getStatusIcon,
		infoToast,
		successToast
	} from '$lib/duui/utils/ui'

	import {
		faArrowDownWideShort,
		faArrowLeft,
		faArrowUpWideShort,
		faCancel,
		faChevronLeft,
		faClockRotateLeft,
		faFileDownload,
		faFileUpload,
		faFilter,
		faListCheck,
		faRepeat,
		faSearch,
		faTrash
	} from '@fortawesome/free-solid-svg-icons'
	import {
		ProgressBar,
		getDrawerStore,
		getModalStore,
		getToastStore,
		type DrawerSettings
	} from '@skeletonlabs/skeleton'

	import Search from '$lib/svelte/components/Input/Search.svelte'
	import Select from '$lib/svelte/components/Input/Select.svelte'
	import KeyValue from '$lib/svelte/components/KeyValue.svelte'
	import Paginator from '$lib/svelte/components/Paginator.svelte'
	import { showConfirmationModal } from '$lib/svelte/utils/modal'
	import { onMount } from 'svelte'
	import Fa from 'svelte-fa'

	export let data
	const toastStore = getToastStore()

	let { pipeline, process } = data
	let documents: DUUIDocument[] = []
	let count: number = 0

	let progressPercent: number = 0

	let tableHeader: string[] = ['Name', 'Progress', 'Status', 'File Size', 'Duration']

	let paginationSettings: PaginationSettings = {
		page: 0,
		limit: 10,
		total: count,
		sizes: [5, 10, 20, 50]
	}

	let sort: Sort = {
		index: 0,
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
	let maxProgress = process.size || pipeline.components.length

	const UPDATE_INTERVAL = 1_000
	let interval: NodeJS.Timeout

	onMount(() => {
		async function update() {
			const response = await fetch(`/api/processes?process_id=${process.oid}`, {
				method: 'GET'
			})

			if (response.ok) {
				try {
					process = await response.json()
					progressPercent = progresAsPercent(process.progress, process.document_names.length)
				} catch (err) {}
				updateTable()
			}

			if (!isActive(process.status)) {
				clearInterval(interval)
			}

			if (progressPercent > 100) progressPercent = 100
			if (process.is_finished) {
				clearInterval(interval)
			}
		}

		if (!process.is_finished) {
			interval = setInterval(update, UPDATE_INTERVAL)
		}
		update()
		return () => clearInterval(interval)
	})

	const cancelProcess = async () => {
		const response = await fetch('/api/processes', {
			method: 'PUT',
			body: JSON.stringify({ oid: process.oid })
		})
		process.status = Status.Cancelled
		process.is_finished = true

		if (response.ok) {
			toastStore.trigger(successToast('Process has been cancelled'))
		} else {
			toastStore.trigger(infoToast('Process has already been cancelled'))
		}
	}

	const deleteProcess = async () => {
		const confirm = await showConfirmationModal(
			{
				title: 'Delete process',
				message: 'Are you you want to delete this process?',
				textYes: 'Delete'
			},
			modalStore
		)

		if (!confirm) return

		const response = await fetch('/api/processes', {
			method: 'DELETE',
			body: JSON.stringify({ oid: process.oid })
		})

		if (response.ok) {
			toastStore.trigger(infoToast('Process has been deleted'))
			goto(`/pipelines/${pipeline.oid}?tab=1`)
		}
	}

	async function restart() {
		goto(
			`/processes?pipeline_id=${pipeline.oid}
			&from=/processes/${process.oid}
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
			`/api/processes/documents
				?process_id=${process.oid}
				&limit=${paginationSettings.limit}
				&skip=${paginationSettings.limit * paginationSettings.page}
				&sort=${sortMap.get(sort.index)}
				&order=${sort.order}
				&search=${searchText}
				&status=${filter.join(';')}`,
			{
				method: 'GET'
			}
		)

		if (response.ok) {
			const json: {
				documents: DUUIDocument[]
				count: number
			} = await response.json()

			documents = json.documents
			paginationSettings.total = json.count
		}
	}

	const modalStore = getModalStore()
	const drawerStore = getDrawerStore()

	const showDocumentModal = (document: DUUIDocument) => {
		const drawer: DrawerSettings = {
			id: 'document',
			width: 'w-full lg:w-[60%] h-full',
			position: 'right',
			rounded: 'rounded-none',
			border: 'border-l border-color',
			meta: { process: process, document: document, pipeline: pipeline }
		}

		drawerStore.open(drawer)
	}

	const sortTable = (index: number) => {
		sort.order = sort.index !== index ? 1 : sort.order === 1 ? -1 : 1
		sort.index = index
		updateTable()
	}
</script>

<div class="menu-mobile">
	<a class="button-mobile" href={`/pipelines/${pipeline.oid}?tab=1`}>
		<Fa icon={faArrowLeft} />
		<span>Pipeline</span>
	</a>
	{#if process.is_finished}
		<button class="button-mobile" on:click={restart}>
			<Fa icon={faRepeat} />
			<span>Restart</span>
		</button>
		<button class="button-mobile" on:click={deleteProcess}>
			<Fa icon={faTrash} />
			<span>Delete</span>
		</button>
	{:else}
		<button class="button-mobile" on:click={cancelProcess}>
			<Fa icon={faCancel} />
			<span>Cancel</span>
		</button>
	{/if}
	<Select
		style="button-mobile hover:!bg-transparent border-0 !flex-col-reverse"
		label="Status"
		name="status-mobile"
		icon={faFilter}
		on:change={updateTable}
		bind:selected={filter}
		options={documentStatusNames}
	/>
</div>

<div>
	<div class="grid isolate">
		<div class="sticky top-0 bg-surface-50-900-token border-b border-color hidden md:block z-[20]">
			<div class="grid grid-cols-3 md:flex items-center md:justify-start relative">
				<a class="anchor-menu border-r border-color" href={`/pipelines/${pipeline.oid}?tab=1`}>
					<Fa icon={faArrowLeft} />
					<span>{pipeline.name}</span>
				</a>
				{#if process.is_finished}
					<button class="button-menu border-r border-color" on:click={restart}>
						<Fa icon={faRepeat} />
						<span>Restart</span>
					</button>
					<button class="button-menu border-l border-color md:ml-auto" on:click={deleteProcess}>
						<Fa icon={faTrash} />
						<span>Delete</span>
					</button>
				{:else}
					<button class="button-menu border-l border-color md:ml-auto" on:click={cancelProcess}>
						<Fa icon={faCancel} />
						<span>Cancel</span>
					</button>
				{/if}
			</div>
		</div>

		<div class="p-4 space-y-4 overflow-x-hidden">
			{#if process.error}
				<p
					class="text-error-500 font-bold p-4 variant-soft-error bordered-soft text-center mx-auto max-w-screen-lg rounded-md"
				>
					ERROR: {process.error}
				</p>
			{/if}
			<div>
				<div
					class="mx-auto grid md:grid-cols-3 items-center md:justify-between h3 section-wrapper p-4 md:divide-x divider
					!border-b-0 !rounded-b-none"
				>
					<div class="flex-center-4 justify-center">
						<Fa
							icon={getStatusIcon(process.status)}
							class={equals(process.status, Status.Active) ? 'animate-spin-slow ' : ''}
						/>
						<p>
							{process.status}
						</p>
					</div>
					<div class="flex-center-4 justify-center">
						<Fa icon={faListCheck} />
						<p>
							{process.progress} / {process.document_names.length} ({progresAsPercent(
								process.progress,
								process.document_names.length
							)}%)
						</p>
					</div>
					<div class="flex-center-4 justify-center">
						<Fa icon={faClockRotateLeft} />
						<p>{getDuration(process.started_at, process.finished_at)}</p>
					</div>
				</div>
				<div class="section-wrapper !border-t-0 !rounded-t-none overflow-hidden">
					<ProgressBar
						value={process.progress}
						max={process.document_names.length}
						height="h-4"
						rounded="!rounded-none"
						track="bg-surface-100-800-token"
						meter="bg-gradient-to-r from-primary-500/50 to-primary-500"
					/>
				</div>
			</div>

			<div>
				<h1 class="h2">Documents</h1>
				<div class="md:text-base flex items-center">
					<div
						class="hidden ml-auto md:flex overflow-hidden justify-between section-wrapper !shadow-none !border-b-0 !rounded-b-none z-10"
					>
						<Search
							style="bg-fancy py-3 "
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
							style="z-50 !rounded-none hidden md:flex "
							border="border-l border-color"
							label="Status"
							name="Status"
							on:change={updateTable}
							bind:selected={filter}
							options={documentStatusNames}
						/>
					</div>
				</div>

				<div class="space-y-4">
					<div class="section-wrapper !rounded-tr-none">
						<div
							class="grid grid-cols-3 lg:grid-cols-5 bg-surface-100-800-token border-b border-color"
						>
							{#each tableHeader as column, index}
								<button
									class="button-neutral border-none !rounded-none !justify-start {index ===
									sort.index
										? 'bg-surface-50-900-token'
										: ''}
										{[3, 4].includes(index) ? '!hidden lg:!inline-flex' : ''}"
									on:click={() => sortTable(index)}
								>
									<span>{column}</span>
									{#if sort.index === index}
										<Fa icon={sort.order === -1 ? faArrowDownWideShort : faArrowUpWideShort} />
									{/if}
								</button>
							{/each}
						</div>

						<div class="overflow-hidden flex flex-col">
							{#each documents as document}
								<button
									class="rounded-none
										grid grid-cols-3 lg:grid-cols-5 gap-8 items-center
										p-4
										hover:variant-filled-primary
										text-xs lg:text-sm text-start"
									on:click={() => showDocumentModal(document)}
								>
									<p>{document.name}</p>
									<div class="md:flex items-center justify-start md:gap-4 text-start">
										<p>
											{Math.round((Math.min(document.progress, maxProgress) / maxProgress) * 100)} %
										</p>
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
									<p class="hidden lg:inline-flex">{formatFileSize(document.size)}</p>
									<p class="hidden lg:inline-flex">
										{formatMilliseconds(getTotalDuration(document))}
									</p>
								</button>
							{/each}
						</div>
					</div>
					<Paginator bind:settings={paginationSettings} on:change={updateTable} />
				</div>
			</div>

			<div class="section-wrapper space-y-8 p-4 !mb-16">
				<h2 class="h3">Process Graph</h2>
				<div
					class="flex flex-col gap-8 items-center relative before:bg-surface-100-800-token isolate before:card
						   before:absolute before:h-full before:w-1 before:-translate-x-1/2 before:left-1/2
						   before:-z-50"
				>
					<div
						class="card px-16 py-8 relative flex justify-center items-center gap-4 flex-col text-center"
					>
						<Fa icon={faFileDownload} size="2x" />
						<p>{process.input.provider}</p>
						<p>{process.document_names.length} Documents</p>
					</div>
					{#each pipeline.components as component}
						<div class="card px-16 py-8 w-modal-slim text-center">
							<p>{component.name}</p>
						</div>
					{/each}
					{#if process.output.provider !== IO.None}
						<div
							class="card px-16 py-8 relative flex justify-center items-center gap-4 flex-col text-center"
						>
							<Fa icon={faFileUpload} size="2x" />
							<p>{process.output.provider}</p>
							<p>{process.document_names.length} Documents</p>
						</div>
					{/if}
				</div>
			</div>

			<div class="section-wrapper space-y-8 p-4 !mb-16">
				<div class="space-y-8">
					<div class="flex justify-between items-center gap-4">
						<h2 class="h3">Settings</h2>
						<button
							class="button-neutral"
							on:click={() =>
								drawerStore.open({
									id: 'process',
									position: 'right',
									rounded: 'rounded-none',
									border: 'border-l border-color',
									width: 'w-full sm:w-1/4',
									meta: { process: process }
								})}
						>
							<Fa icon={faChevronLeft} />
							<span>Json</span>
						</button>
					</div>
					<div class="grid sm:grid-cols-2 lg:grid-cols-4 xl:grid-cols-6 gap-2">
						<KeyValue key="Input" value={process.input.provider} />
						<KeyValue key="Output" value={process.output.provider} />
						{#each Object.entries(process.settings) as [key, value]}
							<KeyValue
								key={snakeToTitleCase(key)}
								value={key === 'minimum_size'
									? formatFileSize(+value)
									: value === true
									? 'Yes'
									: value === false
									? 'No'
									: value}
							/>
						{/each}
						{#if process.initial}
							<KeyValue key="Documents found" value={process.initial} />
							<KeyValue key="Documents skipped" value={process.skipped} />
							<KeyValue key="Documents kept" value={process.initial - process.skipped} />
						{/if}
					</div>
				</div>
			</div>
		</div>
	</div>
</div>
