<script lang="ts">
	import { goto } from '$app/navigation'
	import ActionButton from '$lib/components/ActionButton.svelte'
	import DriverIcon from '$lib/components/DriverIcon.svelte'
	import IconButton from '$lib/components/IconButton.svelte'
	import { API_URL } from '$lib/config.js'
	import {
		Input,
		Output,
		type DUUIDocument,
		isCloudProvider,
		getTotalDuration
	} from '$lib/duui/io.js'
	import { Status, isActive, statusNames } from '$lib/duui/monitor.js'
	import { processToSeachParams, type DUUIProcess } from '$lib/duui/process.js'
	import { Api, makeApiCall } from '$lib/utils/api'
	import { equals, formatFileSize, includes, progresAsPercent } from '$lib/utils/text.js'
	import { formatMilliseconds, getDuration } from '$lib/utils/time.js'
	import {
		documentStatusNames,
		documentStatusNamesString,
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
		faEquals,
		faFile,
		faFileDownload,
		faFileUpload,
		faRefresh,
		faTrash
	} from '@fortawesome/free-solid-svg-icons'
	import {
		getModalStore,
		getToastStore,
		ProgressBar,
		type ModalComponent,
		type ModalSettings,
		type ToastSettings
	} from '@skeletonlabs/skeleton'

	import { onMount } from 'svelte'
	import Fa from 'svelte-fa'

	export let data
	const toastStore = getToastStore()

	let { pipeline, process } = data

	let progressPercent: number = 0

	let documents: DUUIDocument[] = []
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

			const documentResponse = await fetch(
				API_URL +
					'/processes/' +
					process.oid +
					`/documents?status=${statusFilter}&limit=${itemsPerPage}&offset=${itemsPerPage * page}`,
				{
					method: 'GET',
					mode: 'cors'
				}
			)

			if (!documentResponse.ok || !isActive(process.status)) {
				clearInterval(interval)
			}

			const update: DUUIProcess = await response.json()

			documents = (await documentResponse.json()).documents
			process = update

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
				body: `Are you sure you want to delete this run?`,
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

	function getOutput(): string {
		if (equals(process.output.target, Output.Dropbox)) {
			if (process.output.folder.startsWith('/')) {
				return `https://www.dropbox.com/home/Apps/Cedric%20Test%20App${process.output.folder}`
			} else {
				return `https://www.dropbox.com/home/Apps/Cedric%20Test%20App/${process.output.folder}`
			}
		} else if (equals(process.output.target, Output.Minio)) {
			return `http://127.0.0.1:9090/browser/${process.output.folder}`
		}

		return ''
	}

	async function restart() {
		goto(
			`/process?pipeline=${pipeline.oid}
			&from=/process/${process.oid}
			&${processToSeachParams(process)}`
		)
	}

	let filteredDocuments: DUUIDocument[] = []
	let searchText: string = ''

	let sortIndex: number = 0
	let sortOrder: number = 1

	let page: number = 0
	let itemsPerPage: number = 10
	let maxPages: number = documents.length / itemsPerPage
	let sortedDocuments: DUUIDocument[] = documents
	let statusFilter: string = Status.Any

	let maxProgress = pipeline.components.length + (isCloudProvider(process.output.target) ? 1 : 0)

	$: {
		if (documents.length > 0) {
			filteredDocuments = documents.filter(
				(d) =>
					(includes(d.name, searchText) || searchText === '') &&
					(equals(d.status, statusFilter) || statusFilter === '' || statusFilter === Status.Any)
			)

			sortedDocuments = filteredDocuments
				.sort((a, b) => {
					if (sortIndex === 1) return a.progress > b.progress ? sortOrder : -sortOrder
					if (sortIndex === 2)
						return documentStatusNamesString.indexOf(a.status) >
							documentStatusNamesString.indexOf(b.status)
							? sortOrder
							: -sortOrder
					if (sortIndex === 3) return a.size > b.size ? sortOrder : -sortOrder
					if (sortIndex === 4)
						return getTotalDuration(a) > getTotalDuration(b) ? sortOrder : -sortOrder
					return a.name > b.name ? sortOrder : -sortOrder
				})
				.slice(itemsPerPage * page, itemsPerPage * (page + 1))

			maxPages = Math.floor(filteredDocuments.length / itemsPerPage)
			if (filteredDocuments.length % itemsPerPage === 0) maxPages -= 1
		}
	}

	const incrementPage = () => {
		page += 1
	}

	const decrementPage = () => {
		page -= 1
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

	$: {
		const finishedDocuments = documents.filter((d) => equals(d.status, Status.Completed))
		if (finishedDocuments.length > 0) {
			console.log(
				finishedDocuments.reduce((sum, current) => sum + current.processDuration, 0) /
					finishedDocuments.length
			)
		}
	}

	const sortTable = (index: number) => {
		sortOrder = sortIndex !== index ? 1 : (sortOrder *= -1)
		sortIndex = index
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
			<!-- <p>{progressPercent} %</p> -->
		</div>
	</div>
	<hr />
	<div class="flex items-center justify-start gap-4">
		<ActionButton
			icon={faArrowLeft}
			text="Back"
			on:click={() => goto('/pipelines/' + pipeline.oid + '?tab=1')}
		/>
		{#if isCloudProvider(process.input.source)}
			<ActionButton
				text="Input"
				icon={faFileDownload}
				on:click={() => console.log('GO TO INPUT NOT IMPLEMENTED')}
			/>
		{/if}
		{#if isCloudProvider(process.output.target) && equals(process.status, Status.Completed)}
			<ActionButton
				text="Output"
				icon={faFileUpload}
				on:click={() => console.log('GO TO OUTPUT NOT IMPLEMENTED')}
			/>
		{/if}
		{#if !process.finished}
			<ActionButton text="Cancel" icon={faCancel} on:click={cancelProcess} />
		{:else}
			<ActionButton text="Restart" icon={faRefresh} on:click={restart} />
			<ActionButton text="Delete" icon={faTrash} on:click={deleteProcess} />
		{/if}
		<div class="items-center gap-4 hidden md:flex ml-auto">
			<input
				class="input border-1 rounded-md"
				type="text"
				placeholder="Search..."
				bind:value={searchText}
			/>

			<select bind:value={statusFilter} class="input">
				{#each documentStatusNames as status}
					<option value={status}>{status}</option>
				{/each}
			</select>
		</div>
	</div>
	{#if process.error}
		<p class="text-error-400 font-bold break-words">ERROR: {process.error}</p>
	{/if}
	<div class="overflow-hidden rounded-md border-[1px] border-surface-500">
		<div
			class="header grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 variant-soft-surface"
		>
			{#each tableHeader as column, index}
				<button
					class="btn variant-soft-surface gap-4 justify-start items-center rounded-none p-4 text-left
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
		<div class="rounded-md rounded-t-none overflow-hidden flex flex-col">
			{#each sortedDocuments as document, index}
				<button
					class="btn rounded-none {index % 2 === 1 || sortedDocuments.length === 1
						? 'variant-soft-surface'
						: ''} hover:variant-soft-primary grid-cols-2 grid sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-8 p-4 text-left"
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

	<div
		class="flex items-center justify-between gap-4 {documents.length <= itemsPerPage
			? 'hidden'
			: 'block'}"
	>
		<IconButton
			icon={faArrowLeft}
			_class={page > 0 ? 'inline-flex' : 'invisible'}
			on:click={decrementPage}
		/>
		<IconButton
			icon={faArrowRight}
			_class={page < maxPages ? 'inline-flex' : 'invisible'}
			on:click={incrementPage}
		/>
	</div>

	<div class="gap-4 grid items-start">
		<div class="card shadow-lg rounded-md p-4 space-y-4">
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
