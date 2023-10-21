<script lang="ts">
	import { goto } from '$app/navigation'
	import DriverIcon from '$lib/components/DriverIcon.svelte'
	import { API_URL } from '$lib/config.js'
	import { Input, Output, type DUUIDocument, outputIsCloudProvider } from '$lib/duui/io.js'
	import { Status, type DUUIStatusEvent, isActive, getComponentStatus } from '$lib/duui/monitor.js'
	import { processAsSeachParams } from '$lib/duui/process.js'
	import { equals, includes, progresAsPercent } from '$lib/utils/text.js'
	import { getDuration } from '$lib/utils/time.js'
	import { getDocumentStatusIcon, getStatusIcon } from '$lib/utils/ui.js'
	import DocumentModal from './DocumentModal.svelte'

	import {
		faArrowLeft,
		faArrowRight,
		faCancel,
		faCaretDown,
		faCaretUp,
		faFile,
		faFileDownload,
		faFileUpload,
		faGears,
		faRefresh
	} from '@fortawesome/free-solid-svg-icons'
	import {
		getModalStore,
		getToastStore,
		type ModalComponent,
		type ModalSettings,
		type ToastSettings
	} from '@skeletonlabs/skeleton'

	import { onMount } from 'svelte'
	import Fa from 'svelte-fa'

	export let data
	const toastStore = getToastStore()

	let { pipeline, process } = data

	let status: string = process.status
	let error: string = ''
	let progress: number = process.progress
	let progressPercent: number
	let log: DUUIStatusEvent[] = process.log.reverse()
	let done: boolean = false

	let logExpanded: boolean = false

	const inputIsText: boolean = equals(process.input.source, Input.Text)

	let documents: Map<string, DUUIDocument> = new Map()

	onMount(() => {
		async function updateProcess() {
			const response = await fetch(API_URL + '/processes/' + process.id, {
				method: 'GET',
				mode: 'cors'
			})

			if (!response.ok || !isActive(process.status)) {
				clearInterval(interval)
			}

			const updatedProcess = await response.json()
			status = updatedProcess.status
			progress = updatedProcess.progress
			log = updatedProcess.log.reverse()
			error = updatedProcess.error
			done = updatedProcess.done
			process.documentNames = updatedProcess.documentNames || []
			process.documentCount = updatedProcess.documentCount || 0
			process.documents = updatedProcess.documents || []

			process.documentNames.map((document) => {
				if (includes(document, '/')) {
					return document.split('/').at(-1)
				} else {
					return document
				}
			})

			for (let doc of updatedProcess.documentNames) {
				let name = includes(doc, '/') ? doc.split('/').at(-1) : doc

				let document = process.documents.find((d) => d.name === name)
				if (document) {
					documents.set(name, document)
				} else {
					documents.set(name, {
						name: name,
						path: doc,
						progress: 0,
						done: false,
						decodeDuration: 0,
						deserializeDuration: 0,
						processDuration: 0,
						error: ''
					})
				}
			}


			progressPercent = progresAsPercent(
				progress,
				inputIsText ? pipeline.components.length : process.documentCount
			)

			if (progressPercent > 100) progressPercent = 100

			if (done) {
				clearInterval(interval)
			}
		}

		const interval = setInterval(updateProcess, 500)
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
		status = Status.Canceled
		done = true
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
		goto(`/process?pipeline=${pipeline.id}&${processAsSeachParams(process)}`)
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
</script>

<div class="p-4 mx-auto container grid gap-4">
	<div class="space-y-4 card shadow-lg rounded-md p-4">
		<div class="flex justify-between items-center gap-4">
			<h3 class="h3">{pipeline.name}</h3>
	
			<div class="flex items-center gap-4">
				<Fa
					icon={getStatusIcon(status)}
					size="lg"
					class={equals(status, Status.Running) ? 'animate-spin-slow ' : ''}
				/>
				<p class="h4">{status}</p>
			</div>
		</div>
		<hr />
		<div class="flex flex-col gap-4">
			<div class="flex justify-between items-center gap-4">
				<h3 class="h3">{inputIsText ? 'Components' : 'Documents'}</h3>
				<p class="h4">{progressPercent} %</p>
			</div>
			<div class="grid md:grid-cols-2 xl:grid-cols-3 2xl:grid-cols-4 grid-flow-row gap-4">
				{#each documents.values() as document}
					<button
						on:click={() => showDocumentModal(document)}
						class="btn variant-soft-surface flex gap-2 justify-start"
					>
						<Fa
							icon={getDocumentStatusIcon(document, status, inputIsText)}
							size="lg"
							class={equals(status, Status.Running) && !document.done ? 'animate-spin-slow' : ''}
						/>
						<p class=" grow">{document.name}</p>

						<p class="">
							{inputIsText
								? !isActive(status) && !equals(status, Status.Failed)
									? pipeline.components.length
									: process.progress
								: document.progress}
							/ {pipeline.components.length +
								(outputIsCloudProvider(process.output.target) ? 1 : 0)}
						</p>
					</button>
				{/each}

			</div>
		</div>


		<hr />
		<div class="flex flex-col md:flex-row justify-between gap-4">
			<button class="btn variant-ghost-success" on:click={() => goto('/pipelines/' + pipeline.id)}>
				<span><Fa icon={faArrowLeft} /></span>
				<span>Back to pipeline</span>
			</button>
			<!-- outputIsCloudProvider(process.output.type) &&  -->
			{#if done && !equals(process.output.target, Output.None) && !(equals(status, Status.Failed) || equals(status, Status.Canceled))}
				<a href={getOutput()} target="_blank" class="btn variant-ghost-primary">
					<span><Fa icon={faFile} /></span>
					<span>Get Output</span>
				</a>
			{/if}
			{#if !done}
				<button class="btn variant-ghost-error" on:click={cancelPipeline}>
					<span><Fa icon={faCancel} /></span>
					<span>Cancel Pipeline</span>
				</button>
			{:else}
				<button on:click={restart} class="btn variant-ghost-primary mr-auto">
					<span><Fa icon={faRefresh} /></span>
					<span>Restart</span>
				</button>
			{/if}
		</div>
	</div>

	<div class="gap-4 grid md:grid-cols-2 items-start">
		<div class="card shadow-lg rounded-md p-4 space-y-4">
			<h3 class="h3">Pipeline status</h3>

			<hr />
			{#if error}
				<p class="text-error-500 font-bold break-words">ERROR: {error}</p>
			{/if}
			{#each pipeline.components as component}
				<div class="flex gap-4 items-center p-4">
					<DriverIcon driver={component.settings.driver} />
					<p class="h4 grow">{component.name}</p>
					<p class="h4">{getComponentStatus(log, component)}</p>
				</div>
			{/each}
		</div>
		<div class="card shadow-lg rounded-md p-4 space-y-4">
			<div class="flex justify-between items-center gap-4">
				<h3 class="h3">Log</h3>
				<div class="flex gap-16 justify-between items-center">
					<div
						class="flex gap-4 items-center rounded-md overflow-hidden variant-soft-surface p-2 px-4"
					>
						<Fa icon={faFileDownload} size="md" />
						<p class="">{process.input.source}</p>
					</div>
					<Fa icon={faArrowRight} size="lg" />
					<div
						class="flex gap-4 items-center rounded-md overflow-hidden variant-soft-surface p-2 px-4"
					>
						<Fa icon={faFileUpload} size="md" />
						<p class="">{process.output.target}</p>
					</div>
				</div>
				<button
					class="btn variant-ghost-primary flex items-center gap-4"
					on:click={() => (logExpanded = !logExpanded)}
				>
					<span><Fa icon={logExpanded ? faCaretUp : faCaretDown} /></span>
					<span>{logExpanded ? 'Hide log' : 'Show log'}</span>
				</button>
			</div>
			{#if logExpanded}
				<hr />
				<div class="space-y-2">
					{#each log as statusEvent}
						<div class="flex items-start gap-8">
							<p class="min-w-[8ch]">+{getDuration(process.startedAt, statusEvent.timestamp)}</p>
							<p class="break-words max-w-[20ch] md:max-w-[60ch]">{statusEvent.message}</p>
						</div>
					{/each}
				</div>
			{/if}
		</div>
	</div>
</div>
