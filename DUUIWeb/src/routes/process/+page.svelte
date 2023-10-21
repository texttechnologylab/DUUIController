<script lang="ts">
	import { goto } from '$app/navigation'
	import { page } from '$app/stores'
	import { API_URL } from '$lib/config.js'
	import {
		Input,
		InputFileExtensions,
		InputSources,
		Output,
		OutputFileExtensions,
		OutputTargets,
		isValidIO,
		isValidS3BucketName,
		outputIsCloudProvider
	} from '$lib/duui/io.js'
	import { needsAuthorization } from '$lib/duui/user.js'
	import { equals } from '$lib/utils/text.js'
	import { faQuestion, faQuestionCircle } from '@fortawesome/free-solid-svg-icons'

	import { Step, Stepper, type PopupSettings, popup } from '@skeletonlabs/skeleton'
	import Fa from 'svelte-fa'

	export let data
	let { dbxAuthorized, session, dbxURL } = data

	let inputSource: string = $page.url.searchParams.get('input-source') || 'Text'
	let inputFolder: string = $page.url.searchParams.get('input-folder') || ''
	let inputContent: string = $page.url.searchParams.get('input-content') || 'Hello World!'
	let inputFileExtension: string = $page.url.searchParams.get('input-file-extension') || '.txt'

	let outputTarget: string = $page.url.searchParams.get('output-target') || 'None'
	let outputFolder: string = $page.url.searchParams.get('output-folder') || ''
	let outputFileExtension: string = $page.url.searchParams.get('output-file-extension') || '.txt'

	$: input = {
		source: inputSource,
		folder: inputFolder,
		content: inputContent,
		fileExtension: inputFileExtension
	}

	$: output = {
		target: outputTarget,
		folder: outputFolder,
		fileExtension: outputFileExtension
	}

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
		let response = await fetch(API_URL + '/processes', {
			method: 'POST',
			mode: 'cors',
			headers: {
				session: session || ''
			},
			body: JSON.stringify({
				pipeline_id: $page.url.searchParams.get('pipeline'),
				input: {
					source: inputSource,
					folder: inputFolder,
					content: inputContent,
					fileExtension: inputFileExtension
				},
				output: {
					target: outputTarget,
					folder: outputFolder,
					fileExtension: outputFileExtension
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

	let inputBucketIsValid: boolean = false
	let outputBucketIsValid: boolean = false

	$: {
		inputBucketIsValid = !equals(inputSource, Input.Minio) || isValidS3BucketName(input.folder)
		outputBucketIsValid = !equals(outputTarget, Output.Minio) || isValidS3BucketName(output.folder)

	}

	const s3NamingPopup: PopupSettings = {
		event: 'hover',
		target: 's3NamingPopup',
		placement: 'top',
		middleware: {
			offset: 24
		}
	}
</script>

<div
	class="variant-filled-primary hidden p-4 rounded-md shadow-lg space-y-2 text-md z-100"
	data-popup="s3NamingPopup"
>
	<p class="h5">S3 Bucket Naming Conventions</p>
	<ul class="list-disc px-4">
		<li>Bucket names must be between 3 (min) and 63 (max) characters long.</li>
		<li>Bucket names can consist only of lowercase letters, numbers, dots (.), and hyphens (-).</li>
		<li>Bucket names must begin and end with a letter or number.</li>
		<li>Bucket names must not contain two adjacent periods.</li>
		<li>Bucket names must not be formatted as an IP address (for example, 192.168.5.4).</li>
		<li>Bucket names must not start with the prefix xn--.</li>
		<li>Bucket names must not start with the prefix sthree- and the prefix sthree-configurator.</li>
		<li>Bucket names must not end with the suffix -s3alias.</li>
		<li>Bucket names must not end with the suffix --ol-s3.</li>
	</ul>
	<div class="arrow variant-filled-primary" />
</div>

<h1 class="h1 text-center mx-auto my-4">New Process</h1>
<Stepper
	on:complete={submitProcess}
	buttonCompleteLabel="Start"
	class="max-w-7xl mx-auto p-4"
	active="rounded-full variant-filled px-4"
	badge="rounded-full variant-filled-primary"
>
	<Step locked={!isValidIO(input, output)}>
		<svelte:fragment slot="header">Input & Output</svelte:fragment>
		<div class=" grid md:grid-cols-2 gap-4">
			<div class="card rounded-md p-4 flex gap-4 flex-col">
				<h3 class="h3">Input</h3>
				<label class="label space-y-2">
					<span>Source</span>
					<select class="select border-2" bind:value={inputSource}>
						{#each InputSources as source}
							<option value={source}>{source}</option>
						{/each}
					</select>
				</label>
				<!-- {#if inputSource === DUUIDocumentSource.Files}
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
					{/if} -->
				{#if equals(inputSource, 'Text')}
					<label class="label space-y-2">
						<span>Document Text</span>
						<textarea
							class="textarea border-2"
							rows="4"
							placeholder="Enter the document text"
							bind:value={inputContent}
						/>
					</label>
				{:else}
					{#if equals(input.source, 'minio')}
						<label class="label space-y-2">
							<div class="flex items-center gap-2">
								<span class={!inputBucketIsValid ? 'text-error-500' : ''}>Bucket Name</span>
								<div class="[&>*]:pointer-events-none" use:popup={s3NamingPopup}>
									<Fa icon={faQuestionCircle} />
								</div>
							</div>
							<input
								class="input border-2 "
								type="text"
								bind:value={inputFolder}
							/>
						</label>
					{:else}
						<label class="label space-y-2">
							<span>Path to folder</span>
							<input class="input border-2" type="text" bind:value={inputFolder} />
						</label>
					{/if}

					<label class="label space-y-2">
						<span>File extension</span>
						<select class="select border-2 input" bind:value={inputFileExtension}>
							{#each InputFileExtensions as extension}
								<option value={extension}>{extension}</option>
							{/each}
						</select>
					</label>
				{/if}
			</div>
			<div class="card rounded-md p-4 flex gap-4 flex-col">
				<h3 class="h3">Output</h3>
				<label class="label space-y-2">
					<span>Target</span>
					<select class="select border-2" bind:value={outputTarget}>
						{#each OutputTargets as target}
							<option value={target}>{target}</option>
						{/each}
					</select>
				</label>
				{#if outputIsCloudProvider(outputTarget)}
					{#if equals(output.target, 'minio')}
						<label class="label space-y-2">
							<div class="flex items-center gap-2">
								<span class={!outputBucketIsValid ? 'text-error-500' : ''}>Bucket Name</span>
								<div class="[&>*]:pointer-events-none" use:popup={s3NamingPopup}>
									<Fa icon={faQuestionCircle} />
								</div>
							</div>
							<input class="input border-2" type="text" bind:value={outputFolder} />
						</label>
					{:else}
						<label class="label space-y-2">
							<span>Path to folder</span>
							<input class="input border-2" type="text" bind:value={outputFolder} />
						</label>
					{/if}
				{/if}
				{#if !equals(outputTarget, 'none')}
					<label class="label space-y-2">
						<span>File extension</span>
						<select class="select border-2 input" bind:value={outputFileExtension}>
							{#each OutputFileExtensions as extension}
								<option value={extension}>{extension}</option>
							{/each}
						</select>
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
	<Step locked={!dbxAuthorized && needsAuthorization(inputSource, outputTarget)}>
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

		{#if !dbxAuthorized && needsAuthorization(inputSource, outputTarget)}
			<a href={dbxURL.toString()} target="_blank" class="btn variant-filled-primary">
				Authenticate Dropbox
			</a>
		{/if}
	</Step>
</Stepper>
