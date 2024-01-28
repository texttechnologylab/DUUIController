<script lang="ts">
	import type { DUUIDocument } from '$lib/duui/io'
	import { Status } from '$lib/duui/monitor'
	import type { DUUIProcess } from '$lib/duui/process'
	import { datetimeToString } from '$lib/duui/utils/text'
	import { getDuration } from '$lib/duui/utils/time'

	export let process: DUUIProcess
	export let document: DUUIDocument

	let documentStart = process.started_at + document.duration_wait
	let documentDeserializeStart = documentStart + document.duration_decode
	let documentProcessStart = documentDeserializeStart + document.duration_deserialize

	let steps: { name: string; time: number }[] = [
		{
			name: 'Process Started',
			time: process.started_at
		},
		{
			name: 'Document Loaded',
			time: documentStart
		},
		{
			name: 'Document Decoded',
			time: documentDeserializeStart
		},
		{
			name: 'Document Deserialized',
			time: documentProcessStart
		}
	]

	for (let event of document.events) {
		if (event.event.message.startsWith(document.path + ' is being processed by component')) {
			const componentName = event.event.message
				.split(document.path + ' is being processed by component ')
				.at(-1)

			steps.push({
				name: componentName + ' Started',
				time: event.timestamp
			})
		}

		if (event.event.message.startsWith(document.path + ' has been processed by component')) {
			const componentName = event.event.message
				.split(document.path + ' has been processed by component ')
				.at(-1)

			steps.push({
				name: componentName + ' Finished',
				time: event.timestamp
			})
		}
	}
</script>

<div class="section-wrapper p-4 space-y-4">
	{#each steps as step, index}
		<div class="input-wrapper max-w-lg p-4">
			{#if index === 0}
				<p>
					{new Date(step.time).toUTCString()}
				</p>
			{:else}
				<p>
					{'+ ' + getDuration(steps.at(index - 1).time, step.time)}
				</p>
			{/if}
			<p>{step.name}</p>
		</div>
	{/each}
</div>
