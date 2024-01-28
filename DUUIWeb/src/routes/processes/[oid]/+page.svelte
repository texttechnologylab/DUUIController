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
	import DocumentModal from '../../../lib/svelte/widgets/modal/DocumentModal.svelte'

	import {
		faArrowDownWideShort,
		faArrowLeft,
		faArrowUpWideShort,
		faCancel,
		faCheckDouble,
		faClose,
		faCopy,
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
		clipboard
	} from '@skeletonlabs/skeleton'

	import Search from '$lib/svelte/widgets/input/Search.svelte'
	import Select from '$lib/svelte/widgets/input/Select.svelte'
	import Paginator from '$lib/svelte/widgets/navigation/Paginator.svelte'
	import { onMount } from 'svelte'
	import Fa from 'svelte-fa'
	import DriverIcon from '$lib/svelte/DriverIcon.svelte'
	import Number from '$lib/svelte/widgets/input/Number.svelte'

	export let data
	const toastStore = getToastStore()

	let { pipeline, process, documents, pipelineProgress, count } = data

	let progressPercent: number = 0

	let tableHeader: string[] = ['Path', 'Progress', 'Status', 'File Size', 'Duration']

	let paginationSettings: PaginationSettings = {
		page: 0,
		limit: 10,
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
	let maxProgress = pipeline.components.length

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

			const response = await fetch('/api/processes', {
				method: 'DELETE',
				body: JSON.stringify({ oid: process.oid })
			})
			if (response.ok) {
				toastStore.trigger(infoToast('Process has been deleted'))
				goto(`/pipelines/${pipeline.oid}`)
			}
		})
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
				&text=${searchText}
				&filter=${filter.join(';')}`,
			{
				method: 'GET'
			}
		)
		const json: {
			documents: DUUIDocument[]
			pipelineProgress: { [key: number]: number }
			count: number
		} = await response.json()

		documents = json.documents
		paginationSettings.total = json.count
		pipelineProgress = json.pipelineProgress
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
			meta: { process: process, document: document }
		}
		modalStore.trigger(modal)
	}

	const sortTable = (index: number) => {
		sort.order = sort.by !== index ? 1 : sort.order === 1 ? -1 : 1
		sort.by = index
		updateTable()
	}

	$: pipelineStatus = new Map<string, string>(
		process.pipeline_status ? Object.entries(process.pipeline_status) : Object.entries({})
	)
</script>

<div class="menu-mobile">
	<a class="button-mobile" href={`/pipelines/${pipeline.oid}`}>
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
</div>

<div>
	<div class="grid">
		<div
			class="sticky top-0 bg-surface-50-900-token border-y p-4 border-color hidden md:block z-10"
		>
			<div class="grid grid-cols-3 md:flex items-center md:justify-start gap-4 relative">
				<a class="button-primary" href={`/pipelines/${pipeline.oid}`}>
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
		<div class="p-4 space-y-4">
			<div class="section-wrapper p-4 grid md:grid-cols-[1fr_1fr] gap-8">
				<div class="space-y-2">
					<div class="flex items-center gap-4 justify-between">
						<h2 class="h3">Status</h2>
						<Fa
							size="2x"
							icon={getStatusIcon(process.status)}
							class={equals(process.status, Status.Active) ? 'animate-spin-slow ' : ''}
						/>
					</div>
					<div class="grid md:grid-cols-2 items-center justify-start">
						<div>
							<p>{process.status}</p>
							<p>Total process duration: {getDuration(process.started_at, process.finished_at)}</p>
							<p>
								{process.progress} of {process.document_names.length} Documents have been processed.
							</p>
						</div>

						{#if process.initial}
							<div class="hidden md:grid grid-cols-2 gap-2">
								<p>Documents found</p>
								<p>{process.initial}</p>
								<p>Documents skipped</p>
								<p>{process.skipped}</p>
								<p>Documents kept</p>
								<p>{process.initial - process.skipped}</p>
							</div>
						{/if}
					</div>
				</div>
				<div class="space-y-2">
					<div class="flex items-center justify-between gap-2">
						<h2 class="h3">Settings</h2>
						<button use:clipboard={JSON.stringify(process.settings, null, 2)}>
							<Fa icon={faCopy} size="2x" />
						</button>
					</div>
					<div class="grid grid-cols-4 md:grid-cols-6 gap-2">
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
					</div>
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
								<p class="break-words">{document.path}</p>
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
				<Paginator bind:settings={paginationSettings} on:change={updateTable} />
			</div>

			<div class="section-wrapper space-y-8 p-4 !mb-32">
				<div class="max-w-md gap-4">
					{#if process.pipeline_status}
						<div class="space-y-4">
							<h2 class="h2 mb-8">Pipeline Status</h2>
							{#each Object.entries(process.pipeline_status) as [key, status]}
								{#if key.endsWith('Driver')}
									<div class="input-wrapper p-4 grid grid-cols-[1fr_1fr] gap-2">
										<p>{key}</p>
										<p class="ml-auto badge variant-soft-primary">{status}</p>
									</div>
								{/if}
							{/each}

							{#each pipeline.components as component, index}
								<div
									class="input-wrapper p-4 flex justify-between gap-4 items-start text-sm md:text-base"
								>
									<div class="grid items-center gap-4 grow">
										<div class="flex items-center gap-4">
											<DriverIcon driver={component.driver} />
											<p>{component.name}</p>
										</div>
									</div>
									<p class="badge variant-soft-primary">
										{pipelineStatus.get(component.name)}
									</p>
								</div>
							{/each}
						</div>
					{/if}
				</div>
			</div>
		</div>
	</div>
</div>
