<script lang="ts">
	import {
		URLFromProvider,
		getTotalDuration,
		type DUUIDocument,
		type DUUIDocumentProvider
	} from '$lib/duui/io'
	import { formatFileSize } from '$lib/duui/utils/text'
	import { formatMilliseconds } from '$lib/duui/utils/time'
	import { scrollIntoView } from '$lib/duui/utils/ui'
	import { faCheckDouble, faWarning } from '@fortawesome/free-solid-svg-icons'
	import { getModalStore } from '@skeletonlabs/skeleton'
	import { onMount } from 'svelte'
	import Fa from 'svelte-fa'

	export let input: DUUIDocumentProvider
	export let output: DUUIDocumentProvider
	const modalStore = getModalStore()

	let document: DUUIDocument = $modalStore[0].meta.document

	let URLIn: string = URLFromProvider(input) + '/' + document.path
	let URLOut: string = URLFromProvider(output) + '/' + document.path

	onMount(() => {
		scrollIntoView('scroll-top')
	})
</script>

<div class="section-wrapper container max-w-6xl sticky top-0 max-h-[80vh] !overflow-y-auto">
	<div id="scroll-top" />
	<div
		class="font-bold text-2xl p-4 border-surface-200 dark:border-surface-500 text-center flex items-center justify-between sticky top-0 z-10 gradient"
	>
		<p>{document.name}</p>
		{#if document.error}
			<Fa icon={faWarning} size="lg" class="text-error-500" />
		{:else if document.finished}
			<Fa icon={faCheckDouble} size="lg" class="text-success-500" />
		{/if}
	</div>

	<div class="p-4 space-y-8">
		{#if document.error}
			<p class="section-wrapper text-lg text-error-500 font-bold p-4">
				{document.error}
			</p>
		{/if}
		<div
			class="grid grid-cols-2 md:grid-cols-4 justify-center items-center section-wrapper text-sm"
		>
			<div
				class="bg-fancy flex flex-col items-start justify-center gap-2 border-r border-surface-200 dark:border-surface-500 p-4"
			>
				<p class="font-bold">Status</p>
				<p>{document.status}</p>
			</div>
			{#if URLIn}
				<a
					href={URLIn}
					target="_blank"
					class="bg-fancy flex flex-col items-start justify-center gap-2 border-r border-surface-200 dark:border-surface-500 p-4"
				>
					<p class="anchor font-bold">Source</p>
					<p>{input.provider}</p>
				</a>
			{:else}
				<div
					class="bg-fancy flex flex-col items-start justify-center gap-2 border-r border-surface-200 dark:border-surface-500 p-4"
				>
					<p class="font-bold">Source</p>
					<p>{input.provider}</p>
				</div>
			{/if}
			{#if URLOut}
				<a
					href={URLOut}
					target="_blank"
					class="bg-fancy flex flex-col items-start justify-center gap-2 border-r border-surface-200 dark:border-surface-500 p-4"
				>
					<p class="anchor font-bold">Target</p>
					<p>{output.provider}</p>
				</a>
			{:else}
				<div
					class="bg-fancy flex flex-col items-start justify-center gap-2 border-r border-surface-200 dark:border-surface-500 p-4"
				>
					<p class="font-bold">Target</p>
					<p>{output.provider}</p>
				</div>
			{/if}
			<div class="bg-fancy flex flex-col items-start justify-center gap-2 p-4">
				<p class="font-bold">Size</p>
				<p>{document.size ? formatFileSize(document.size) : 'Unknown'}</p>
			</div>
		</div>
		<div class="grid grid-cols-3 justify-center items-center section-wrapper rounded-md text-sm">
			<div
				class="bg-fancy flex flex-col items-start justify-center gap-2 border-r border-surface-200 dark:border-surface-500 p-4"
			>
				<p class="font-bold">Setup</p>
				<p>{formatMilliseconds(document.decodeDuration + document.deserializeDuration)}</p>
			</div>
			<div
				class="bg-fancy flex flex-col items-start justify-center gap-2 border-r border-surface-200 dark:border-surface-500 p-4"
			>
				<p class="font-bold">Wait</p>
				<p>{formatMilliseconds(document.waitDuration)}</p>
			</div>
			<div class="bg-fancy flex flex-col items-start justify-center gap-2 p-4">
				<p class="font-bold">Process</p>
				<p>{formatMilliseconds(document.processDuration)}</p>
			</div>
			<p
				class="bg-fancy col-span-3 border-t border-surface-200 dark:border-surface-500 p-4 text-center font-bold text-lg"
			>
				Total {formatMilliseconds(getTotalDuration(document))}
			</p>
		</div>
		{#if document.annotations}
			<div class="section-wrapper text-sm">
				{#each Object.entries(document.annotations) as entry}
					<div class="grid md:grid-cols-2 border-b border-surface-200 dark:border-surface-500">
						<div class="bg-fancy flex md:flex-col items-start justify-center gap-2 p-4">
							<p class="font-bold">Class</p>
							<p class="">{entry[0].split('.').slice(-1)}</p>
						</div>
						<div class="bg-fancy flex md:flex-col items-start justify-center gap-2 p-4">
							<p class="font-bold">Count</p>
							<p>{entry[1]}</p>
						</div>
						<div
							class="bg-fancy md:flex flex-col items-start justify-center gap-2 p-4 col-span-2 hidden"
						>
							<p class="font-bold">Annotation</p>
							<p class="">{entry[0]}</p>
						</div>
					</div>
				{/each}
				<!-- <pre class="text-sm">{JSON.stringify(document.annotations, null, 2)}</pre> -->
			</div>
		{/if}

		<!-- <Chart
					{data}
					type="pie"
					title="Processing Step Duration (ms)"
					colors={['#006c98', '#4d98b7', '#006189', '#005172']}
				/> -->
	</div>
</div>
