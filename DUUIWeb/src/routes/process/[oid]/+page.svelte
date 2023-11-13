<script lang="ts">
	import { goto, invalidate, invalidateAll } from '$app/navigation'
	import ActionButton from '$lib/svelte/widgets/action/ActionButton.svelte'
	import DriverIcon from '$lib/components/DriverIcon.svelte'
	import IconButton from '$lib/svelte/widgets/action/IconButton.svelte'
	import { API_URL } from '$lib/config.js'
	import {
		Output,
		type DUUIDocument,
		isCloudProvider,
		getTotalDuration,
		Input
	} from '$lib/duui/io.js'
	import { Status, isActive } from '$lib/duui/monitor.js'
	import { processToSeachParams, type DUUIProcess } from '$lib/duui/process.js'
	import ComboBox from '$lib/svelte/widgets/input/ComboBox.svelte'
	import TextInput from '$lib/svelte/widgets/input/TextInput.svelte'
	import { Api, makeApiCall } from '$lib/utils/api'
	import { equals, formatFileSize, includes, progresAsPercent } from '$lib/utils/text.js'
	import { formatMilliseconds } from '$lib/utils/time.js'
	import {
		documentStatusNames,
		getDocumentStatusIcon,
		getStatusIcon,
		info,
		success
	} from '$lib/utils/ui.js'
	import DocumentModal from './DocumentModal.svelte'

	import {
		faArrowDownWideShort,
		faArrowLeft,
		faArrowRight,
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
	import { page } from '$app/stores'
	import Anchor from '$lib/svelte/widgets/action/Anchor.svelte'
	import { browser } from '$app/environment'

	export let data
	const toastStore = getToastStore()

	let { pipeline, process, documentQuery } = data

	let documents: DUUIDocument[] = documentQuery.documents
	let total: number = documentQuery.total

	let progressPercent: number = 0

	let tableHeader: string[] = ['Name', 'Progress', 'Status', 'File Size', 'Duration']

	onMount(() => {
		async function updateProcess() {
			const response = await fetch(API_URL + '/processes/' + process.oid, {
				method: 'GET',
				mode: 'cors'
			})

			if (!response.ok || !isActive(process.status)) {
				clearInterval(interval)
			}

			const documentsResponse = await makeApiCall(
				Api.Documents,
				'GET',
				{},
				`oid=${process.oid}&limit=${itemsPerPage}&skip=${
					pageIndex * itemsPerPage
				}&sort=${sortMap.get(
					sortIndex
				)}&order=${sortOrder}&text=${searchText}&status=${statusFilters.join(';')}`
			)

			if (!documentsResponse.ok || !isActive(process.status)) {
				clearInterval(interval)
			}

			const result = await documentsResponse.json()
			documents = result.documents
			total = result.total

			const update: DUUIProcess = await response.json()

			process = update
			maxPages = Math.ceil(total / itemsPerPage)

			progressPercent = progresAsPercent(process.progress, process.documentNames.length)

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
				type: 'confirm',
				title: 'Delete Run',
				buttonTextConfirm: 'Delete',
				body: `Are you sure you want to delete this process?`,
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

	let searchText: string = ''

	const sortMap: Map<number, string> = new Map([
		[0, 'name'],
		[1, 'progress'],
		[2, 'status'],
		[3, 'size'],
		[4, 'duration']
	])

	let sortTerm: string = $page.url.searchParams.get('sort') || 'name'
	let sortOrder: number = $page.url.searchParams.get('order') === '-1' ? -1 : 1
	let sortIndex: number = 0

	for (let value of sortMap.values()) {
		if (value === sortTerm) {
			sortIndex = ([...sortMap].find(([k, v]) => v === value) || [0])[0]
		}
	}

	let itemsPerPage: number = Math.min(+($page.url.searchParams.get('limit') || '10'), 10)
	let pageIndex: number = Math.max(
		0,
		Math.ceil(+($page.url.searchParams.get('skip') || '0') / itemsPerPage)
	)

	let maxPages: number = Math.ceil(total / itemsPerPage)

	let statusFilters: string[] = [Status.Any]

	let maxProgress = pipeline.components.length + (isCloudProvider(process.output.target) ? 1 : 0)

	const incrementPage = async () => {
		if (total <= (pageIndex + 1) * itemsPerPage) return
		pageIndex += 1
		updateResults(true)
	}

	const decrementPage = async () => {
		if (pageIndex === 0) return
		pageIndex -= 1
		updateResults(true)
	}

	const updateResults = async (navigation: boolean = false) => {
		if (!browser) return
		goto(
			`/process/${process.oid}?limit=${itemsPerPage}&skip=${
				pageIndex * itemsPerPage
			}&sort=${sortMap.get(
				sortIndex
			)}&order=${sortOrder}&text=${searchText}&status=${statusFilters.join(';')}`
		)

		if (!navigation) {
			pageIndex = 0
		}

		const documentsResponse = await makeApiCall(
			Api.Documents,
			'GET',
			{},
			`oid=${process.oid}&limit=${itemsPerPage}&skip=${pageIndex * itemsPerPage}&sort=${sortMap.get(
				sortIndex
			)}&order=${sortOrder}&text=${searchText}&status=${statusFilters.join(';')}`
		)

		const result = await documentsResponse.json()
		documents = result.documents
		total = result.total
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
		sortOrder = sortIndex !== index ? 1 : (sortOrder *= -1)
		sortIndex = index
		updateResults()
	}

	$: {
		const lastFilter: string | undefined = statusFilters.at(-1)

		if (equals(lastFilter || '', Status.Any) || statusFilters.length === 0) {
			statusFilters = [Status.Any]
		} else {
			statusFilters = statusFilters.filter((status) => !equals(status, Status.Any))
		}

		updateResults()
	}
</script>

<div class="container h-full flex-col mx-auto flex gap-4 md:my-8">
	<div class="flex items-end justify-between gap-4">
		<h1 class="h2">{pipeline.name}</h1>

		<div class="flex items-center gap-4 ml-auto">
			<Fa
				icon={getStatusIcon(process.status)}
				size="lg"
				class={equals(process.status, Status.Running) ? 'animate-spin-slow ' : ''}
			/>
			<p>{process.status}</p>
		</div>
	</div>
	<hr />
	<div class="grid xl:flex items-center xl:justify-start gap-4">
		<div class="xl:flex grid grid-cols-2 gap-4 items-center">
			<ActionButton
				icon={faArrowLeft}
				text="Back"
				on:click={() => goto('/pipelines/' + pipeline.oid + '?tab=2')}
			/>
			{#if isCloudProvider(process.input.source)}
				<ActionButton text="Input" icon={faFileDownload} on:click={() => window.open(getInput())} />
			{/if}
			{#if isCloudProvider(process.output.target) && equals(process.status, Status.Completed)}
				<ActionButton text="Output" icon={faFileUpload} on:click={() => window.open(getOutput())} />
			{/if}
			{#if !process.finished}
				<ActionButton text="Cancel" icon={faCancel} on:click={cancelProcess} />
			{:else}
				<ActionButton text="Restart" icon={faRefresh} on:click={restart} />
				<ActionButton text="Delete" icon={faTrash} on:click={deleteProcess} />
			{/if}
		</div>

		<div class="grid md:flex items-center gap-4 lg:ml-auto">
			<TextInput
				classes="grow"
				bind:value={searchText}
				placeholder="Search..."
				icon={faSearch}
				on:focusout={() => updateResults()}
				on:keydown={(event) => {
					if (equals(event.key, 'enter')) {
						updateResults()
					}
				}}
			/>
			<ComboBox
				id="status"
				bind:values={statusFilters}
				options={documentStatusNames}
				multiple={true}
				text="Status"
				closeQuery="button"
			/>
		</div>
	</div>
	{#if process.error}
		<p class="text-error-400 font-bold break-words">ERROR: {process.error}</p>
	{/if}
	<div class="overflow-hidden border-[1px] border-surface-700">
		<div
			class="header grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 variant-soft-surface"
		>
			{#each tableHeader as column, index}
				<button
					class="btn variant-soft-surface gap-4 justify-start items-center rounded-none p-3 text-left
					{[1].includes(index)
						? 'hidden sm:inline-flex'
						: [4].includes(index)
						? 'hidden md:inline-flex'
						: [3].includes(index)
						? 'hidden lg:inline-flex'
						: ''}"
					on:click={() => sortTable(index)}
				>
					<span>{column}</span>
					{#if sortIndex === index}
						<Fa icon={sortOrder === -1 ? faArrowDownWideShort : faArrowUpWideShort} />
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
			{#each documents as document, index}
				<button
					class="btn rounded-none {index % 2 === 1 || documents.length === 1
						? 'variant-soft-surface'
						: ''} hover:variant-soft-primary grid-cols-2 grid sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-8 p-2 text-left"
					on:click={() => showDocumentModal(document)}
				>
					<p>{document.name}</p>
					<p class="hidden sm:flex items-center gap-4">
						<span
							>{Math.round((Math.min(document.progress, maxProgress) / maxProgress) * 100)} %</span
						>
						<span>{Math.min(document.progress, maxProgress)} / {maxProgress}</span>
					</p>
					<p class="flex justify-start items-center gap-4">
						<Fa
							icon={getDocumentStatusIcon(document)}
							size="lg"
							class={equals(document.status, Status.Running)
								? 'animate-spin-slow'
								: equals(document.status, Status.Waiting)
								? 'animate-hourglass'
								: ''}
						/>
						<span>{document.status}</span>
					</p>
					<p class="hidden lg:block">{formatFileSize(document.size)}</p>
					<p class="hidden md:block">{formatMilliseconds(getTotalDuration(document))}</p>
				</button>
			{/each}
		</div>
	</div>

	<div class="flex items-center justify-center gap-4 variant-soft-surface mx-auto">
		<IconButton rounded="rounded-none" icon={faArrowLeft} on:click={decrementPage} />
		{#if total === 0}
			<p>No results</p>
		{:else}
			<p>
				{1 + pageIndex * itemsPerPage}-{Math.min((pageIndex + 1) * itemsPerPage, total)} of {total}
			</p>
		{/if}
		<IconButton rounded="rounded-none" icon={faArrowRight} on:click={incrementPage} />
	</div>

	<div class="gap-4 grid items-start">
		<div class="variant-soft-surface shadow-lg p-4 space-y-4">
			<h3 class="h3">Pipeline status</h3>

			<hr />
			{#each pipeline.components as component}
				<div class="flex gap-4 items-center p-4">
					<DriverIcon driver={component.settings.driver} />
					<div>
						<p class="h4 grow">{component.name}</p>
						<p class="h4">{component.status}</p>
					</div>
				</div>
			{/each}
		</div>
	</div>
</div>
