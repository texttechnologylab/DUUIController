<script lang="ts">
	import { goto } from '$app/navigation'
	import DriverIcon from '$lib/components/DriverIcon.svelte'
	import { API_URL } from '$lib/config.js'
	import type { DUUIComponent } from '$lib/duui/component.js'
	import { Input, Output, outputIsCloudProvider } from '$lib/duui/io.js'
	import {
		Status,
		type DUUIStatusEvent,
		isActive,
		documentIsProcessed,
		isDocumentProcessed,
		getDocumentProgress
	} from '$lib/duui/monitor.js'
	import { processAsSeachParams } from '$lib/duui/process.js'
	import { equals, includes, progresAsPercent } from '$lib/utils/text.js'
	import { getDuration } from '$lib/utils/time.js'
	import { getDocumentStatusIcon, getStatusIcon } from '$lib/utils/ui.js'

	import {
		faArrowLeft,
		faArrowRight,
		faCancel,
		faCaretDown,
		faCaretUp,
		faCheck,
		faCheckDouble,
		faFile,
		faRefresh,
		faX
	} from '@fortawesome/free-solid-svg-icons'
	import { getToastStore, type ToastSettings } from '@skeletonlabs/skeleton'

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

	let searchText: string = ''
	let documentStatus: string = 'All'
	let filteredDocuments: string[] = process.documentNames
	let filteredComponents: DUUIComponent[] = pipeline.components

	let documentProgress: Map<string, number> = new Map(
		Object.entries(process.documentProgress || {})
	)

	let componentCategory: string = 'All'
	let componentCategories: string[] = ['All'].concat(pipeline.components.map((c) => c.category))

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
			documentProgress = new Map(Object.entries(updatedProcess.documentProgress || {}))
			done = updatedProcess.done
			process.documentNames = updatedProcess.documentNames || []
			process.documentCount = updatedProcess.documentCount || 0

			process.documentNames.map((document) => {
				if (includes(document, '/')) {
					return document.split('/').at(-1)
				} else {
					return document
				}
			})

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

	$: {
		if (inputIsText) {
			filteredComponents = pipeline.components.filter((component) => {
				if (equals('All', componentCategory)) {
					return includes(component.name, searchText)
				} else {
					return (
						includes(component.name, searchText) && equals(component.category, componentCategory)
					)
				}
			})
		} else {
			filteredDocuments = process.documentNames.filter((document) => {
				return includes(document, searchText)

				// if (documentStatus === 'All') {

				// } else {
				// 	return document.toLowerCase().includes(searchText.toLowerCase())
				// }
			})
		}
	}
</script>

<div class="p-4 mx-auto container grid gap-4">
	<div class="space-y-4 card shadow-lg rounded-md p-4">
		<div class="flex justify-between items-center gap-4">
			<h3 class="h3">{pipeline.name}</h3>
			<div class="grid md:grid-cols-2 gap-4">
				<input class="input border-2" placeholder="Search" type="text" bind:value={searchText} />
				{#if inputIsText}
					<select class="select border-2 input" bind:value={componentCategory}>
						{#each componentCategories as category}
							<option value={category}>{category}</option>
						{/each}
					</select>
				{:else}
					<select class="select border-2 input" bind:value={documentStatus}>
						{#each ['All', 'Running', 'Success', 'Failed'] as status}
							<option value={status}>{status}</option>
						{/each}
					</select>
				{/if}
			</div>
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
			{#if inputIsText}
				{#each filteredComponents as component, id}
					<div class="flex gap-4 items-center px-4">
						{#if progress > id}
							<Fa icon={faCheckDouble} size="lg" />
						{:else if isActive(status)}
							<Fa icon={faRefresh} size="lg" class="animate-spin-slow " />
						{:else}
							<Fa icon={faX} size="lg" />
						{/if}
						<DriverIcon driver={component.settings.driver} />
						<p class="h4 grow">{component.name}</p>
					</div>
				{/each}
			{:else if filteredDocuments}
				<div class="grid md:grid-cols-2 xl:grid-cols-3 2xl:grid-cols-5 grid-flow-row gap-4">
					{#each filteredDocuments as document}
						<div class="flex gap-4 items-center px-4">
							<Fa
								icon={getDocumentStatusIcon(documentProgress, status, process, pipeline, document)}
								size="lg"
								class={isActive(status) &&
								!isDocumentProcessed(documentProgress, process, pipeline, document)
									? 'animate-spin-slow'
									: ''}
							/>
							<p>
								{getDocumentProgress(documentProgress, pipeline, document)}
							</p>
							<p>{document.split('/').at(-1)}</p>
						</div>
					{/each}
				</div>
			{/if}
		</div>

		<hr />
		<div class="flex flex-col md:flex-row justify-between gap-4">
			<button class="btn variant-ghost-success" on:click={() => goto('/pipelines/' + pipeline.id)}>
				<span><Fa icon={faArrowLeft} /></span>
				<span>Back to pipeline</span>
			</button>
			<!-- outputIsCloudProvider(process.output.type) &&  -->
			{#if done && !equals(process.output.target, Output.None)}
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

	<div class="space-y-4 card shadow-lg rounded-md p-4">
		<div class="flex justify-between items-center gap-4">
			<h3 class="h3">Log</h3>
			<div class="flex gap-4 items-center">
				<p>Reading from {process.input.source}</p>
				<div class="flex justify-center">
					<Fa icon={faArrowRight} size="sm" />
				</div>
				<p>Writing to {process.output.target}</p>
			</div>
			<button
				class="btn variant-soft-surface flex items-center gap-4"
				on:click={() => (logExpanded = !logExpanded)}
			>
				<span><Fa icon={logExpanded ? faCaretUp : faCaretDown} /></span>
				<span>{logExpanded ? 'Hide log' : 'Show log'}</span>
			</button>
		</div>
		<hr />
		<div class="space-y-2">
			{#if error}
				<p class="text-error-500 font-bold">ERROR: {error}</p>
			{/if}

			{#if logExpanded}
				{#each log as statusEvent}
					<div class="flex items-start gap-8">
						<p class="min-w-[8ch]">+{getDuration(process.startedAt, statusEvent.timestamp)}</p>
						<p class="break-words max-w-[20ch] md:max-w-[60ch]">{statusEvent.message}</p>
					</div>
				{/each}
			{/if}
		</div>
	</div>
</div>
