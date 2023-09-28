<script lang="ts">
	import { DUUIStatus } from '$lib/data.js'
	import { pipelineActive, toTitleCase } from '$lib/utils.js'
	import { faCancel } from '@fortawesome/free-solid-svg-icons'
	import { ProgressRadial } from '@skeletonlabs/skeleton'

	import { onMount } from 'svelte'
	import Fa from 'svelte-fa'

	export let data

	let { pipeline, process } = data

	let status = process.status
	let progress = process.progress

	// function cancelPipeline() {
	// 	if (status === DUUIStatus.Completed) {
	// 		const t: ToastSettings = {
	// 			message: 'Pipeline has already completed',
	// 			timeout: 4000,
	// 			background: 'variant-filled-warning'
	// 		}
	// 		getToastStore().trigger(t)
	// 		return
	// 	}

	// 	if (status === DUUIStatus.Cancelled) {
	// 		const t: ToastSettings = {
	// 			message: 'Pipeline has already cancelled',
	// 			timeout: 4000,
	// 			background: 'variant-filled-warning'
	// 		}
	// 		getToastStore().trigger(t)
	// 		return
	// 	}

	// 	fetch('http://127.0.0.1:2605/processes/' + process.id, {
	// 		method: 'PUT',
	// 		mode: 'cors'
	// 	})

	// 	const t: ToastSettings = {
	// 		message: 'Pipeline has been cancelled',
	// 		timeout: 4000,
	// 		background: 'variant-filled-warning'
	// 	}
	// 	getToastStore().trigger(t)
	// }

	onMount(() => {
		async function checkStatus() {
			const response = await fetch('http://127.0.0.1:2605/processes/' + process.id, {
				method: 'GET',
				mode: 'cors'
			})

			const content = await response.json()
			status = content.status
			progress = content.progress

			console.log(progress)

			if (status === DUUIStatus.Completed || status === DUUIStatus.Failed) {
				clearInterval(interval)
			}
		}

		// async function checkStatus() {
		// 	if (process === undefined) {
		// 		return
		// 	}
		// 	if (process.status === DUUIStatus.Completed) {
		// 		return
		// 	}

		// 	const response = await fetch('http://127.0.0.1:2605/processes/' + process_id, {
		// 		method: 'GET',
		// 		mode: 'cors'
		// 	})

		// 	const process = await response.json()
		// 	status = process.status
		// 	progress = process.progress
		// 	building = process.status === DUUIStatus.Setup

		// 	if (process.status !== DUUIStatus.Running || process.status !== DUUIStatus.Setup) {
		// 		clearInterval(interval)
		// 		processes = [...processes, process]
		// 		tableSource = processes.map((process, index, array) => {
		// 			return {
		// 				positon: index,
		// 				status: toTitleCase(process.status),
		// 				progress: (process.progress / pipeline.components.length) * 100 + ' %',
		// 				startedAt: process.startedAt ? toDateTimeString(new Date(process.startedAt)) : '',
		// 				duration: getDuration(process),
		// 				process: process
		// 			}
		// 		})
		// 		tableSource = tableSource
		// 		tableData = {
		// 			head: ['Status', 'Progress', 'Start Time', 'Duration'],
		// 			body: tableMapperValues(tableSource, ['status', 'progress', 'startedAt', 'duration']),
		// 			meta: tableMapperValues(tableSource, ['process'])
		// 		}
		// 	}
		// }

		const interval = setInterval(checkStatus, 2000)
		checkStatus()

		return () => clearInterval(interval)
	})
</script>

<div class="p-4 variant-ghost-surface mx-auto container grid grid-cols-2 gap-4 m-32">
	<div class="flex flex-col gap-4">
		<p>{pipeline.name}</p>
		<p>{toTitleCase(status)}</p>
		<p>{progress}</p>
	</div>
	<div>
		{#if pipelineActive(status)}
			<form class="flex items-center gap-4" on:submit|preventDefault={() => console.log('Cancel')}>
				<ProgressRadial stroke={120} width="w-16" meter="stroke-primary-400" />
				<!-- <button class="btn variant-ghost-error" on:click={cancelPipeline}>
					<span><Fa icon={faCancel} /></span>
					<span>Cancel Pipeline</span>
				</button>  -->
			</form>
		{/if}
	</div>
</div>
