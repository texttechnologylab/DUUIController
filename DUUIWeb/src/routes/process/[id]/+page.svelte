<script lang="ts">
	import { goto } from '$app/navigation'
	import DriverIcon from '$lib/components/DriverIcon.svelte'
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
	import { equals, formatFileSize, includes, progresAsPercent } from '$lib/utils/text.js'
	import { formatMilliseconds, getDuration } from '$lib/utils/time.js'
	import {
		documentStatusNames,
		documentStatusNamesString,
		getDocumentStatusIcon,
		getStatusIcon
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
		faRefresh
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

	const inputIsText: boolean = equals(process.input.source, Input.Text)

	let documents: DUUIDocument[] = []

	onMount(() => {
		async function updateProcess() {
			const response = await fetch(API_URL + '/processes/' + process.id, {
				method: 'GET',
				mode: 'cors'
			})

			if (!response.ok || !isActive(process.status)) {
				clearInterval(interval)
			}

			const documentResponse = await fetch(
				API_URL +
					'/processes/' +
					process.id +
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

	async function cancelPipeline() {
		await fetch(API_URL + '/processes/' + process.id, {
			method: 'PUT',
			mode: 'cors'
		})
		const t: ToastSettings = {
			message: 'Pipeline has been canceled',
			timeout: 4000,
			background: 'variant-filled-warning'
		}
		toastStore.trigger(t)
		process.status = Status.Canceled
		process.finished = true
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
		goto(`/process?pipeline=${pipeline.id}&${processToSeachParams(process)}`)
	}

	let filteredDocuments: DUUIDocument[] = []
	let searchText: string = ''

	let sortIndex: number = 0
	let order: number = 1

	let page: number = 0
	let itemsPerPage: number = 10
	let maxPages: number = documents.length / itemsPerPage
	let sortedDocuments: DUUIDocument[] = documents
	let statusFilter: string = Status.Any

	// const sort = (index: number) => {
	// 	if (index === sortIndex) {
	// 		order *= -1
	// 	} else {
	// 		order = 1
	// 	}
	// 	sortIndex = index

	// 	sortedDocuments = documents
	// 		.slice(itemsPerPage * page, itemsPerPage * (page + 1))
	// 		.sort((a, b) => {
	// 			if (sortIndex === 1) return a.progress > b.progress ? order : -order
	// 			if (sortIndex === 2) return a.status > b.status ? order : -order
	// 			if (sortIndex === 3) return a.size > b.size ? order : -order
	// 			return a.name > b.name ? order : -order
	// 		})
	// }

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
					if (sortIndex === 1) return a.progress > b.progress ? order : -order
					if (sortIndex === 2)
						return documentStatusNamesString.indexOf(() => a.status) >
							documentStatusNamesString.indexOf(() => b.status)
							? order
							: -order
					if (sortIndex === 3) return a.size > b.size ? order : -order
					if (sortIndex === 4) return getTotalDuration(a) > getTotalDuration(b) ? order : -order
					return a.name > b.name ? order : -order
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
			console.log(finishedDocuments.reduce((sum, current) => sum + current.processDuration, 0) / finishedDocuments.length)
		}
	}
</script>

<div class="p-4 mx-auto container grid gap-4">
	<div class="space-y-4 card shadow-lg rounded-md p-4">
		<div class="flex justify-between items-center gap-4">
			<h3 class="h3">{pipeline.name}</h3>
			<!-- <p>{getDuration(process.startTime, process.endTime)}</p> -->

			<div class="flex items-center gap-4">
				<Fa
					icon={getStatusIcon(process.status)}
					size="lg"
					class={equals(process.status, Status.Running) ? 'animate-spin-slow ' : ''}
				/>
				<p>{process.status}</p>
				<p>{progressPercent} %</p>
			</div>
		</div>
		<hr />
		<div class="flex items-center justify-between gap-4">
			<button
				class="btn variant-soft-surface"
				on:click={() => goto('/pipelines/' + pipeline.id + '?tab=1')}
			>
				<span><Fa icon={faArrowLeft} /></span>
				<span>Back</span>
			</button>

			<div class="flex items-center shrink gap-4">
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

			{#if process.finished && !equals(process.output.target, Output.None) && !(equals(process.status, Status.Failed) || equals(process.status, Status.Canceled))}
				<a href={getOutput()} target="_blank" class="btn variant-soft-primary ml-auto">
					<span><Fa icon={faFile} /></span>
					<span>Get Output</span>
				</a>
			{/if}
			{#if !process.finished}
				<button class="btn variant-soft-error ml-auto" on:click={cancelPipeline}>
					<span><Fa icon={faCancel} /></span>
					<span>Cancel</span>
				</button>
			{:else}
				<button on:click={restart} class="btn variant-soft-primary">
					<span>Restart</span>
					<span><Fa icon={faRefresh} /></span>
				</button>
			{/if}
		</div>

		<div class="overflow-hidden">
			<ProgressBar
				value={process.progress}
				max={process.documentNames.length}
				height="h-1"
				rounded="rounded-none"
				meter="variant-filled-primary"
			/>
			<div
				class="header grid grid-cols-3 md:grid-cols-4 lg:grid-cols-5 variant-soft-surface rounded-md shadow-lg mb-2"
			>
				<button
					class="btn variant-soft-surface gap-4 justify-start items-center rounded-none"
					on:click={() => {
						order = sortIndex !== 0 ? 1 : (order *= -1)
						sortIndex = 0
					}}
					>Name {#if sortIndex === 0}
						<Fa icon={order === -1 ? faArrowDownWideShort : faArrowUpWideShort} />
					{/if}</button
				>
				<button
					class="btn variant-soft-surface gap-4 justify-start items-center rounded-none"
					on:click={() => {
						order = sortIndex !== 1 ? 1 : (order *= -1)
						sortIndex = 1
					}}
					>Progress {#if sortIndex === 1}
						<Fa icon={order === -1 ? faArrowDownWideShort : faArrowUpWideShort} />
					{/if}</button
				>
				<button
					class="btn variant-soft-surface gap-4 justify-start items-center rounded-none"
					on:click={() => {
						order = sortIndex !== 2 ? 1 : (order *= -1)
						sortIndex = 2
					}}
					>Status {#if sortIndex === 2}
						<Fa icon={order === -1 ? faArrowDownWideShort : faArrowUpWideShort} />
					{/if}</button
				>
				<button
					class="hidden lg:inline-flex btn variant-soft-surface gap-4 justify-start items-center rounded-none"
					on:click={() => {
						order = sortIndex !== 3 ? 1 : (order *= -1)
						sortIndex = 3
					}}
					>File Size {#if sortIndex === 3}
						<Fa icon={order === -1 ? faArrowDownWideShort : faArrowUpWideShort} />
					{/if}</button
				>

				<button
					class="hidden md:inline-flex btn variant-soft-surface gap-4 justify-start items-center rounded-none"
					on:click={() => {
						order = sortIndex !== 4 ? 1 : (order *= -1)
						sortIndex = 4
					}}
					>Duration {#if sortIndex === 4}
						<Fa icon={order === -1 ? faArrowDownWideShort : faArrowUpWideShort} />
					{/if}</button
				>
			</div>

			<div class="rounded-md overflow-hidden flex flex-col">
				{#each sortedDocuments as document}
					<button
						class="btn hover:variant-soft-primary grid grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-8 p-2 border-b-[1px] border-b-surface-700 text-left"
						on:click={() => showDocumentModal(document)}
					>
						<p>{document.name}</p>
						<p>{Math.min(document.progress, maxProgress)} / {maxProgress}</p>
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
							<span class="hidden md:block">{document.status}</span>
						</p>
						<p class="hidden lg:block">{formatFileSize(document.size)}</p>
						<p class="hidden md:block">{formatMilliseconds(getTotalDuration(document))}</p>
					</button>
				{/each}
			</div>
			<!-- <p class="h4">
				{process.progress} / {process.documentNames.length} finished ({progressPercent} %)
			</p> -->
			<!-- <div
				class="grid grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-8 p-2 border-b-[1px] border-b-surface-700 text-left variant-soft-primary"
			>
				<p class="col-start-3 flex justify-start items-center gap-4 ml-2">
					<Fa icon={faEquals} size="lg" /><span>
						{filteredDocuments.length}
					</span>
				</p>
			</div> -->
		</div>

		<div
			class="flex items-center justify-between gap-4 {documents.length <= itemsPerPage
				? 'hidden'
				: 'block'}"
		>
			<button
				class="btn-icon variant-soft-primary rounded-md shadow-lg
			{page > 0 ? 'inline-flex' : 'invisible'}"
				on:click={decrementPage}
			>
				<Fa icon={faArrowLeft} />
			</button>

			<button
				class="btn-icon variant-soft-primary rounded-md shadow-lg
			{page < maxPages ? 'inline-flex' : 'invisible'}"
				on:click={incrementPage}
			>
				<Fa icon={faArrowRight} />
			</button>
		</div>
	</div>

	<div class="gap-4 grid items-start">
		<div class="card shadow-lg rounded-md p-4 space-y-4">
			<h3 class="h3">Pipeline status</h3>

			<hr />
			{#if process.error}
				<p class="text-error-400 font-bold break-words">ERROR: {process.error}</p>
			{/if}
			{#each pipeline.components as component}
				<div class="flex gap-4 items-center p-4">
					<DriverIcon driver={component.settings.driver} />
					<p class="h4 grow">{component.name}</p>
					<p class="h4">{component.status}</p>
				</div>
			{/each}
		</div>
	</div>
</div>
