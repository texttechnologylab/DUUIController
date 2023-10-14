<script lang="ts">
	import { goto } from '$app/navigation'
	import DriverIcon from '$lib/components/DriverIcon.svelte'
	import { API_URL } from '$lib/config.js'
	import { DUUIDocumentOutput, DUUIDocumentSource, DUUIStatus } from '$lib/data.js'
	import {
		documentIsProcessed,
		getIconForStatus,
		getProgressPercentLive,
		getTimeDifference,
		pipelineActive,
		toTitleCase
	} from '$lib/utils.js'
	import {
		faArrowLeft,
		faCancel,
		faCaretDown,
		faCaretUp,
		faCheck,
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

	let status = process.status
	let error: string = ''
	let progress = process.progress
	let progressPercent: number
	let log = process.log
	let logExpanded: boolean = false

	const inputIsText: boolean = process.input.source === DUUIDocumentSource.Text.toLowerCase()

	let filteredDocuments: string[] = []

	onMount(() => {
		async function checkStatus() {
			const response = await fetch(API_URL + '/processes/' + process.id, {
				method: 'GET',
				mode: 'cors'
			})

			const content = await response.json()
			status = content.status
			progress = content.progress
			log = content.log
			error = content.error

			process.documentNames = content.documentNames || []
			process.documentCount = content.documentCount || 0

			progressPercent = getProgressPercentLive(
				progress,
				inputIsText ? pipeline.components.length : process.documentCount
			)

			if (progressPercent > 100) progressPercent = 100

			if (
				status === DUUIStatus.Completed ||
				status === DUUIStatus.Failed ||
				status === DUUIStatus.Cancelled
			) {
				setTimeout(checkStatus, 5000)
			}
		}

		const interval = setInterval(checkStatus, 500)
		checkStatus()
		return () => clearInterval(interval)
	})

	async function cancelPipeline() {
		await fetch(API_URL + '/processes/' + process.id, {
			method: 'PUT',
			mode: 'cors'
		})
		const t: ToastSettings = {
			message: 'Pipeline has been cancelled',
			timeout: 4000,
			background: 'variant-filled-warning'
		}
		toastStore.trigger(t)
	}

	function getOutput(): string {
		if (process.output.type === DUUIDocumentOutput.Dropbox.toLowerCase()) {
			if (process.output.path.startsWith('/')) {
				return `https://www.dropbox.com/home/Apps/Cedric%20Test%20App${process.output.path}`
			} else {
				return `https://www.dropbox.com/home/Apps/Cedric%20Test%20App/${process.output.path}`
			}
		} else if (process.output.type === DUUIDocumentOutput.Minio.toLowerCase()) {
			return `https://play.min.io:9443/browser/${process.output.path}`
		}

		return ''
	}

	async function restart() {
		goto(
			`/process?pipeline=${pipeline.id}&
			source=${process.input.source}&
			input-path=${process.input.path}&
			${process.input.source === DUUIDocumentSource.Text ? 'text=' + process.input.text + '&' : ''}
			extension=${process.input.extension}&
			type=${process.output.type}&
			output-path=${process.output.path}
			`
		)
	}
	
</script>

<div class="p-4 mx-auto container grid gap-4">
	<div class="space-y-4 card shadow-lg rounded-md p-4">
		<div class="flex justify-between items-center gap-4">
			<h3 class="h3">{pipeline.name}</h3>
			<div class="flex items-center gap-4">
				<Fa
					icon={getIconForStatus(status)}
					size="lg"
					class={status === DUUIStatus.Running ? 'animate-spin-slow ' : ''}
				/>
				<p class="h4">{toTitleCase(status)}</p>
			</div>
		</div>
		<hr />
		<div class="flex flex-col gap-4">
			<div class="flex justify-between items-center gap-4">
				<h3 class="h3">{inputIsText ? 'Components' : 'Documents'}</h3>
				<p class="h4">{progressPercent} %</p>
			</div>
			{#if inputIsText}
				{#each pipeline.components as component, id}
					<div class="flex gap-4 items-center px-4">
						{#if progress > id}
							<Fa icon={faCheck} size="lg" />
						{:else if status === DUUIStatus.Running}
							<Fa icon={faRefresh} size="lg" class="animate-spin-slow " />
						{/if}
						<DriverIcon driver={component.settings.driver} />
						<p class="h4 grow">{component.name}</p>
					</div>
				{/each}
			{:else if process.documentNames}
				<div class="grid md:grid-cols-2 xl:grid-cols-3 2xl:grid-cols-5 grid-flow-row gap-4">
					{#each process.documentNames as document}
						<div class="flex gap-4 items-center px-4">
							{#if pipelineActive(status)}
								{#if documentIsProcessed(log, document)}
									<Fa icon={faCheck} size="lg" class="text-success-400" />
								{:else}
									<Fa icon={faRefresh} size="lg" class="animate-spin-slow" />
								{/if}
							{:else if documentIsProcessed(log, document)}
								<Fa icon={faCheck} size="lg" class="text-success-400" />
							{:else}
								<Fa icon={faX} size="lg" class="text-error-400" />
							{/if}
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
			{#if status === DUUIStatus.Completed && process.output.type !== DUUIDocumentOutput.None.toLowerCase()}
				<a href={getOutput()} target="_blank" class="btn variant-ghost-primary">
					<span><Fa icon={faFile} /></span>
					<span>Get Output</span>
				</a>
			{/if}
			{#if pipelineActive(status)}
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
			{#if error}
				<p class="text-error-500 font-bold">ERROR: {error}</p>
			{/if}
			<button class="flex items-center gap-4" on:click={() => (logExpanded = !logExpanded)}>
				<span><Fa icon={logExpanded ? faCaretUp : faCaretDown} /></span>
				<span>{logExpanded ? 'Hide full log' : 'Show full log'}</span>
			</button>
		</div>
		<hr />
		<div class="space-y-2">
			{#each log.reverse().slice(0, logExpanded ? -1 : 10) as statusEvent}
				<div class="flex items-start gap-8">
					<p class="min-w-[8ch]">+{getTimeDifference(process.startedAt, statusEvent.timestamp)}</p>
					<p class="break-words max-w-[20ch] md:max-w-[60ch]">{statusEvent.message}</p>
				</div>
			{/each}
		</div>
	</div>
</div>
