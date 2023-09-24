<script lang="ts">
	import {
		DUUIDocumentOutput,
		DUUIDocumentSource,
		DUUIDocumentSourcesList,
		type DUUIPipeline
	} from '$lib/data'
	import { faX } from '@fortawesome/free-solid-svg-icons'
	import Fa from 'svelte-fa'

	export let pipeline: DUUIPipeline

	let inputSource: DUUIDocumentSource = DUUIDocumentSource.Folder
	let inputPath: string = ''
	let files: FileList
	let inputText: string = ''

	let outputType: DUUIDocumentOutput = DUUIDocumentOutput.None
	let outputPath: string = ''

	$: if (files) {
		for (const file of files) {
			console.log(`${file.name}: ${file.size} bytes`)
		}
	}

	function removeFileFromFileList(index: number) {
		const dt = new DataTransfer()

		for (let i = 0; i < files.length; i++) {
			const file = files[i]
			if (index !== i) dt.items.add(file) // here you exclude the file. thus removing it.
		}

		files = dt.files // Assign the updates list
	}
</script>

<h1 class="h2 text-center mx-auto my-8">New Process</h1>
<div class="mx-auto container grid grid-cols-2 gap-8 items-start">
	<div class="card p-4 space-y-8 rounded-md">
		<h2 class="h3">Input</h2>
		<form class="space-y-4">
			<label class="label space-y-2">
				<span>Source</span>
				<select class="select" bind:value={inputSource}>
					{#each DUUIDocumentSourcesList as source}
						<option value={source}>{source}</option>
					{/each}
				</select>
			</label>
			{#if inputSource === DUUIDocumentSource.None}
				<label class="label space-y-2">
					<span>Document Text</span>
					<textarea
						class="textarea"
						rows="12"
						placeholder="Enter the document text"
						bind:value={inputText}
					/>
				</label>
			{:else if inputSource === DUUIDocumentSource.Folder}
				<label class="label space-y-2">
					<span>Select files</span>
					<input
						class="input rounded-md text-transparent"
						type="file"
						bind:files
						multiple
						placeholder="Enter the document text"
					/>
				</label>
				{#if files}
					{#each [...files] as file, index}
						<div class="flex justify-between items-center">
							<p>{file.name}</p>
							<button class="btn-icon rounded-md variant-filled-error" on:click={() => removeFileFromFileList(index)}>
								<Fa icon={faX} />
							</button>
						</div>
					{/each}
				{/if}
			{/if}
		</form>
	</div>
	<div class="card p-4 space-y-8 rounded-md">
		<h2 class="h3">Output</h2>
		<form action="" class="space-y-4">
			<label class="label space-y-2">
				<span>Type</span>
				<select class="select" bind:value={outputType}>
					{#each DUUIDocumentSourcesList.slice(1) as source}
						<option value={source}>{source}</option>
					{/each}
				</select>
			</label>
		</form>
	</div>
</div>
