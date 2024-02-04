<script lang="ts">
	import { goto } from '$app/navigation'
	import { getTotalDuration, type DUUIDocument } from '$lib/duui/io.js'
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
	import DocumentModal from '$lib/svelte/components/DocumentDrawer.svelte'

	import {
		faArrowDownWideShort,
		faArrowLeft,
		faArrowUpWideShort,
		faCancel,
		faClockRotateLeft,
		faFilter,
		faListCheck,
		faRefresh,
		faSearch,
		faTrash
	} from '@fortawesome/free-solid-svg-icons'
	import {
		ProgressBar,
		getModalStore,
		getToastStore,
		type ModalComponent,
		type ModalSettings,
		getDrawerStore,
		type DrawerSettings
	} from '@skeletonlabs/skeleton'

	import KeyValue from '$lib/svelte/components/KeyValue.svelte'
	import Paginator from '$lib/svelte/components/Paginator.svelte'
	import Search from '$lib/svelte/components/Search.svelte'
	import Select from '$lib/svelte/components/Select.svelte'
	import { showConfirmationModal } from '$lib/svelte/utils/modal'
	import { onMount } from 'svelte'
	import Fa from 'svelte-fa'
	import { draw } from 'svelte/transition'

	export let data
	const toastStore = getToastStore()

	let { pipeline, process, documents, count } = data

	let progressPercent: number = 0

	let tableHeader: string[] = ['Name', 'Progress', 'Status', 'File Size', 'Duration']

	let paginationSettings: PaginationSettings = {
		page: 0,
		limit: 20,
		total: count,
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
	let maxProgress = process.size || pipeline.components.length

	onMount(() => {
		async function updateProcess() {
			const response = await fetch(`/api/processes?process_id=${process.oid}`, {
				method: 'GET'
			})

			if (!response.ok || !isActive(process.status)) {
				clearInterval(interval)
			}

			process = await response.json()

			progressPercent = progresAsPercent(process.progress, process.document_names.length)
			updateTable()

			if (progressPercent > 100) progressPercent = 100
			if (process.is_finished) {
				clearInterval(interval)
			}
		}
		let interval: NodeJS.Timeout
		if (!process.is_finished) {
			interval = setInterval(updateProcess, 1000)
		}
		updateProcess()
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
				&sort=${sortMap.get(sort.by)}
				&order=${sort.order}
				&search=${searchText}
				&status=${filter.join(';')}`,
			{
				method: 'GET'
			}
		)

		const json: {
			documents: DUUIDocument[]
			count: number
		} = await response.json()

		documents = json.documents
		paginationSettings.total = json.count
	}

	const modalStore = getModalStore()
	const drawerStore = getDrawerStore()

	const showDocumentModal = (document: DUUIDocument) => {
		const drawer: DrawerSettings = {
			id: 'document',
			width: 'w-full h-full',
			position: 'bottom',
			rounded: 'rounded-none',
			meta: { process: process, document: document, pipeline: pipeline }
		}

		drawerStore.open(drawer)
	}

	const sortTable = (index: number) => {
		sort.order = sort.by !== index ? 1 : sort.order === 1 ? -1 : 1
		sort.by = index
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
			<Fa icon={faRefresh} />
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
	<div class="grid">
		<div
			class="sticky top-0 bg-surface-50-900-token border-y p-4 border-color hidden md:block z-10"
		>
			<div class="grid grid-cols-3 md:flex items-center md:justify-start gap-4 relative">
				<a class="button-primary" href={`/pipelines/${pipeline.oid}?tab=1`}>
					<Fa icon={faArrowLeft} />
					<span>Back</span>
				</a>
				{#if process.is_finished}
					<button class="button-primary" on:click={restart}>
						<Fa icon={faRefresh} />
						<span>Restart</span>
					</button>
					<button class="button-error md:ml-auto" on:click={deleteProcess}>
						<Fa icon={faTrash} />
						<span>Delete</span>
					</button>
				{:else}
					<button class="button-error md:ml-auto" on:click={cancelProcess}>
						<Fa icon={faCancel} />
						<span>Cancel</span>
					</button>
				{/if}
			</div>
		</div>
		<div class="p-4 py-8 space-y-4">
			<div class="mx-auto grid md:flex items-center gap-8 justify-center h3">
				<div class="flex items-center gap-4">
					<Fa
						size="lg"
						icon={getStatusIcon(process.status)}
						class={equals(process.status, Status.Active) ? 'animate-spin-slow ' : ''}
					/>
					<p>
						{process.status}
					</p>
				</div>
				<div class="flex items-center gap-4">
					<Fa icon={faListCheck} size="lg" />
					<p>
						{process.progress} / {process.document_names.length} ({progresAsPercent(
							process.progress,
							process.document_names.length
						)}%)
					</p>
				</div>
				<div class="flex items-center gap-4">
					<Fa icon={faClockRotateLeft} size="lg" />
					<p>{getDuration(process.started_at, process.finished_at)}</p>
				</div>
			</div>
			{#if process.error}
				<p class="text-error-500 font-bold p-2 md:max-w-[60ch] max-w-[40ch]">
					ERROR: {process.error}
				</p>
			{/if}
			<div>
				<div class="md:text-base flex items-end">
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

				<div class="section-wrapper !rounded-tr-none">
					<div class="grid grid-cols-3 md:grid-cols-4 lg:grid-cols-5">
						{#each tableHeader as column, index}
							<button
								class="btn-sm md:text-base md:inline-flex px-4 bg-fancy gap-4 justify-start items-center rounded-none p-3 text-left
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
							grid grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-8 p-3 px-4 text-left items-center"
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
								<p class="hidden lg:block">{formatFileSize(document.size)}</p>
								<p class="hidden md:block">{formatMilliseconds(getTotalDuration(document))}</p>
							</button>
						{/each}
					</div>
				</div>
				<Paginator bind:settings={paginationSettings} on:change={updateTable} />
			</div>

			<div class="section-wrapper space-y-8 p-4 !mb-32">
				<div class="space-y-8">
					<h2 class="h3">Settings</h2>
					<div class="grid md:grid-cols-2 lg:grid-cols-4 xl:grid-cols-6 gap-2">
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
