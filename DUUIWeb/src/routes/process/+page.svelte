<script lang="ts">
	import { goto } from '$app/navigation'
	import { page } from '$app/stores'
	import {
		DUUIDocumentOutput,
		DUUIDocumentSource,
		DUUIInputSourcesList,
		DUUIOutputSourcesList
	} from '$lib/data'
	import { cutText, formatFileSize } from '$lib/utils'
	import { faSortNumericAsc, faSortNumericDesc, faX } from '@fortawesome/free-solid-svg-icons'
	import { Step, Stepper } from '@skeletonlabs/skeleton'
	import Fa from 'svelte-fa'

	export let data
	let { session } = data

	let inputSource: DUUIDocumentSource = DUUIDocumentSource.Text
	let inputPath: string = '/txt'
	let inputText: string = 'Hello World.'
	let fileExtension: string = '.txt'

	let outputType: DUUIDocumentOutput = DUUIDocumentOutput.None
	let outputPath: string = '/xd'

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

		data.append(
			'process',
			JSON.stringify({
				pipeline_id: $page.url.searchParams.get('pipeline'),
				input: {
					source: inputSource.toLowerCase(),
					path: inputPath,
					text: inputText,
					extension: fileExtension
				},
				output: {
					type: outputType.toLowerCase(),
					path: outputPath
				}
			})
		)

		let response = await fetch('http://127.0.0.1:2605/processes', {
			method: 'POST',
			mode: 'cors',
			headers: {
				session: session || ''
			},
			body: JSON.stringify({
				pipeline_id: $page.url.searchParams.get('pipeline'),
				input: {
					source: inputSource.toLowerCase(),
					path: inputPath,
					text: inputText,
					extension: fileExtension
				},
				output: {
					type: outputType.toLowerCase(),
					path: outputPath
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
</script>

<h1 class="h2 text-center mx-auto my-8">New Process</h1>
<Stepper
	on:complete={submitProcess}
	class="max-w-7xl mx-auto"
	active="rounded-full variant-filled px-4"
	badge="rounded-full variant-filled-primary"
>
	<Step locked={inputText === DUUIDocumentSource.Text && !inputText}>
		<svelte:fragment slot="header">Select an Input method</svelte:fragment>
		<div class="space-y-4 rounded-md flex justify-center">
			<div class="card p-4 grid gap-4">
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
						<span>{inputSource === DUUIDocumentSource.S3 ? 'Bucket Name' : 'Path to folder'}</span>
						<input class="input border-2" type="text" bind:value={inputPath} />
					</label>
					<label class="label space-y-2">
						<span>File extension</span>
						<select class="select border-2 input" bind:value={fileExtension}>
							{#each ['.txt', '.gz', '.xmi'] as extension}
								<option value={extension}>{extension}</option>
							{/each}
						</select>
					</label>
				{/if}
			</div>
		</div>
	</Step>
	<Step>
		<svelte:fragment slot="header">Select an Output method</svelte:fragment>
		<div class="card p-4 space-y-8 rounded-md">
			<form action="" class="space-y-4">
				<label class="label space-y-2">
					<span>Type</span>
					<select class="select border-2" bind:value={outputType}>
						{#each DUUIOutputSourcesList.slice(1) as source}
							<option value={source}>{source}</option>
						{/each}
					</select>
				</label>
				{#if outputPath !== DUUIDocumentOutput.None}
					<label class="label space-y-2">
						<span>{inputSource === DUUIDocumentSource.S3 ? 'Bucket Name' : 'Path to folder'}</span>
						<input class="input border-2" type="text" bind:value={outputPath} />
					</label>
				{/if}
			</form>
		</div>
	</Step>
</Stepper>
