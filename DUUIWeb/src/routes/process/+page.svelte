<script lang="ts">
	import { goto } from '$app/navigation'
	import { page } from '$app/stores'
	import { API_URL } from '$lib/config.js'
	import {
		DUUIDocumentOutput,
		DUUIDocumentSource,
		DUUIInputSourcesList,
		DUUIOutputSourcesList
	} from '$lib/data'
	import { cutText, formatFileSize, outputIsCloudProvider } from '$lib/utils'
	import { faSortNumericAsc, faSortNumericDesc, faX } from '@fortawesome/free-solid-svg-icons'
	import { Step, Stepper } from '@skeletonlabs/skeleton'
	import Fa from 'svelte-fa'

	export let data
	let { dbxAuthorized, session, dbxURL } = data

	let inputSource: DUUIDocumentSource = inputFromString(
		$page.url.searchParams.get('source') || 'dropbox'
	)
	let inputPath: string = $page.url.searchParams.get('input-path') || '/sample_xmi'
	let inputText: string =
		$page.url.searchParams.get('text') ||
		'Natural language processing (NLP) is an interdisciplinary subfield of computer science and linguistics. It is primarily concerned with giving computers the ability to support and manipulate speech. It involves processing natural language datasets, such as text corpora or speech corpora, using either rule-based or probabilistic (i.e. statistical and, most recently, neural network-based) machine learning approaches.\n\nThe goal is a computer capable of "understanding" the contents of documents, including the contextual nuances of the language within them. The technology can then accurately extract information and insights contained in the documents as well as categorize and organize the documents themselves. Challenges in natural language processing frequently involve speech recognition, natural-language understanding, and natural-language generation.'
	let fileExtension: string = $page.url.searchParams.get('extension') || '.xmi'

	let outputType: DUUIDocumentOutput = outputFromString(
		$page.url.searchParams.get('type') || 'dropbox'
	)
	let outputPath: string = $page.url.searchParams.get('output-path') || '/output/xmi'

	let notify: boolean = false

	let files: FileList
	let sortedFiles: File[] = []

	let sortOrder: number = 1

	function removeFileFromFileList(index: number) {
		const dt = new DataTransfer()

		for (let i = 0; i < files.length; i++) {
			const file = files[i]
			if (index !== i) dt.items.add(file) // here you exclude the file. thus removing it.
		}

		files = dt.files // Assign the updates list
	}

	async function submitProcess() {
		let data = new FormData()
		if (inputSource === DUUIDocumentSource.Files && files) {
			let _files = sortedFiles.length > 0 ? sortedFiles : [...files]
			_files.forEach((file) => data.append('files', file))
		}

		let response = await fetch(API_URL + '/processes', {
			method: 'POST',
			mode: 'cors',
			headers: {
				session: session || ''
			},
			body: JSON.stringify({
				pipeline_id: $page.url.searchParams.get('pipeline'),
				input: {
					source: inputSource.toLowerCase(),
					path: inputPath.toLowerCase(),
					text: inputText,
					extension: fileExtension
				},
				output: {
					type: outputType.toLowerCase(),
					path: outputPath.toLowerCase()
				}
			})
		})

		let content = await response.json()
		if (response.ok) {
			goto('/process/' + content.id)
		}
	}

	function sortFiles() {
		if (!files) {
			return
		}
		sortedFiles = [...files]

		let ascending = sortOrder === 1 ? -1 : 1
		sortedFiles.sort((a, b) => (a.size > b.size ? ascending : -ascending))
		sortOrder = sortOrder === 1 ? -1 : 1
	}

	function needsAuthorization(): boolean {
		return (
			!dbxAuthorized &&
			(inputSource === DUUIDocumentSource.Dropbox || outputType === DUUIDocumentOutput.Dropbox)
		)
	}

	function inputFromString(source: string): DUUIDocumentSource {
		switch (source.toLowerCase()) {
			case 'dropbox':
				return DUUIDocumentSource.Dropbox
			case 's3':
				return DUUIDocumentSource.S3
			case 'minio':
				return DUUIDocumentSource.Minio
			case 'files':
				return DUUIDocumentSource.Files
			default:
				return DUUIDocumentSource.Text
		}
	}

	function outputFromString(type: string): DUUIDocumentOutput {
		switch (type.toLowerCase()) {
			case 'dropbox':
				return DUUIDocumentOutput.Dropbox
			case 's3':
				return DUUIDocumentOutput.S3
			case 'minio':
				return DUUIDocumentOutput.Minio
			case '.json':
				return DUUIDocumentOutput.Json
			case '.txt':
				return DUUIDocumentOutput.Text
			case '.xmi':
				return DUUIDocumentOutput.Xmi
			case '.csv':
				return DUUIDocumentOutput.CSV
			default:
				return DUUIDocumentOutput.None
		}
	}
</script>

<h1 class="h1 text-center mx-auto my-4">New Process</h1>
<Stepper
	on:complete={submitProcess}
	buttonCompleteLabel="Start"
	class="max-w-7xl mx-auto p-4"
	active="rounded-full variant-filled px-4"
	badge="rounded-full variant-filled-primary"
>
	<Step
		locked={(inputText === DUUIDocumentSource.Text && !inputText) ||
			(inputSource === DUUIDocumentSource.Files && !files) ||
			(outputIsCloudProvider(outputType) && !outputPath)}
	>
		<svelte:fragment slot="header">Input & Output</svelte:fragment>
		<div class=" grid md:grid-cols-2 gap-4">
			<div class="card rounded-md p-4 flex gap-4 flex-col">
				<h3 class="h3">Input</h3>
				<label class="label space-y-2">
					<span>Source</span>
					<select class="select border-2" bind:value={inputSource}>
						{#each DUUIInputSourcesList as source}
							<option value={source}>{source}</option>
						{/each}
					</select>
				</label>
				{#if inputSource === DUUIDocumentSource.Files}
					<div class="flex items-end gap-4 justify-start">
						<label class="label space-y-2">
							<span>Select files</span>
							<input
								class="input border-2 text-transparent grow"
								type="file"
								bind:files
								multiple
								placeholder="Enter the document text"
								accept="text/plain, application/gzip, application/xml"
							/>
						</label>
						<button class="btn variant-filled-primary" on:click={sortFiles}>
							<span>Sort by size</span>
							<Fa icon={sortOrder === 1 ? faSortNumericAsc : faSortNumericDesc} />
						</button>
					</div>
					{#if files}
						<div class="space-y-2">
							<p>{files.length} Files</p>
							{#each sortedFiles.length > 0 ? sortedFiles : [...files] as file, index}
								<div class="flex justify-between items-center variant-soft-surface gap-2 p-2 px-4">
									<p>{cutText(file.name, 60)}</p>
									<p class="ml-auto">{formatFileSize(file.size)}</p>
									<button on:click={() => removeFileFromFileList(index)}>
										<Fa icon={faX} />
									</button>
								</div>
							{/each}
						</div>
					{/if}
				{:else if inputSource === DUUIDocumentSource.Text}
					<label class="label space-y-2">
						<span>Document Text</span>
						<textarea
							class="textarea border-2"
							rows="12"
							placeholder="Enter the document text"
							bind:value={inputText}
						/>
					</label>
				{:else}
					<label class="label space-y-2">
						<span
							>{inputSource === DUUIDocumentSource.Minio || inputSource === DUUIDocumentSource.S3
								? 'Bucket Name'
								: 'Path to folder'}</span
						>
						<input class="input border-2" type="text" bind:value={inputPath} />
					</label>
					<label class="label space-y-2">
						<span>File extension</span>
						<select class="select border-2 input" bind:value={fileExtension}>
							{#each ['.txt', '.gz', '.xmi', '.json'] as extension}
								<option value={extension}>{extension}</option>
							{/each}
						</select>
					</label>
				{/if}
			</div>
			<div class="card rounded-md p-4 flex gap-4 flex-col">
				<h3 class="h3">Output</h3>
				<label class="label space-y-2">
					<span>Type</span>
					<select class="select border-2" bind:value={outputType}>
						{#each DUUIOutputSourcesList as source}
							<option value={source}>{source}</option>
						{/each}
					</select>
				</label>
				{#if outputType === DUUIDocumentOutput.S3 || outputType === DUUIDocumentOutput.Dropbox || outputType === DUUIDocumentOutput.Minio}
					<label class="label space-y-2">
						<span
							>{outputType === DUUIDocumentOutput.S3 || outputType === DUUIDocumentOutput.Minio
								? 'Bucket Name (lower case)'
								: 'Path to folder'}</span
						>
						<input class="input border-2" type="text" bind:value={outputPath} />
					</label>
				{:else if outputType !== DUUIDocumentOutput.None}
					<label class="label space-y-2">
						<span>Filename</span>
						<input class="input border-2" type="text" bind:value={outputPath} />
					</label>
				{/if}
			</div>
		</div>
		<svelte:fragment slot="navigation">
			<button
				class="btn variant-filled-error"
				on:click={() => goto('/pipelines/' + $page.url.searchParams.get('pipeline'))}>Cancel</button
			>
		</svelte:fragment>
	</Step>
	<Step locked={needsAuthorization()}>
		<svelte:fragment slot="header">Extra settings</svelte:fragment>
		<div class="card rounded-md p-4 flex gap-4 flex-col">
			<label class="flex items-center space-x-2">
				<span>Get notified when finished (E-Mail)</span>
				<input
					class="checkbox checked:variant-filled-primary"
					type="checkbox"
					bind:value={notify}
				/>
			</label>
		</div>

		{#if needsAuthorization()}
			<a href={dbxURL.toString()} target="_blank" class="btn variant-filled-primary">
				Authenticate Dropbox
			</a>
		{/if}
	</Step>
</Stepper>
