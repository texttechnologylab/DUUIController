<script lang="ts">
	import { goto } from '$app/navigation'
	import { page } from '$app/stores'
	import {
		IO,
		Input,
		InputFileExtensions,
		InputSources,
		OutputFileExtensions,
		OutputTargets,
		areSettingsValid,
		isValidIO,
		isValidInput,
		isValidOutput,
		isValidS3BucketName,
		type DUUIDocumentProvider,
		type FileExtension,
		type IOProvider,
		Output
	} from '$lib/duui/io.js'
	import { Api, makeApiCall } from '$lib/duui/utils/api'
	import { equals, formatFileSize } from '$lib/duui/utils/text'
	import { errorToast, successToast } from '$lib/duui/utils/ui'
	import Checkbox from '$lib/svelte/widgets/input/Checkbox.svelte'
	import Dropdown from '$lib/svelte/widgets/input/Dropdown.svelte'
	import Number from '$lib/svelte/widgets/input/Number.svelte'
	import TextArea from '$lib/svelte/widgets/input/TextArea.svelte'
	import TextInput from '$lib/svelte/widgets/input/TextInput.svelte'
	import { faArrowLeft, faCheck, faClose } from '@fortawesome/free-solid-svg-icons'
	import { FileDropzone, getToastStore } from '@skeletonlabs/skeleton'
	import Fa from 'svelte-fa'

	const toastStore = getToastStore()

	let input: DUUIDocumentProvider = {
		provider: ($page.url.searchParams.get('input-provider') as IOProvider) || 'Text',
		path: $page.url.searchParams.get('input-path') || '',
		content: $page.url.searchParams.get('input-content') || 'Sample Text.',
		fileExtension: ($page.url.searchParams.get('input-file-extension') as FileExtension) || '.txt'
	}

	let output: DUUIDocumentProvider = {
		provider: ($page.url.searchParams.get('output-provider') as IOProvider) || 'None',
		path: $page.url.searchParams.get('output-path') || '',
		content: $page.url.searchParams.get('output-content') || '',
		fileExtension: ($page.url.searchParams.get('output-file-extension') as FileExtension) || '.txt'
	}

	let files: FileList

	let notify: boolean = $page.url.searchParams.get('notify') === 'true' || false
	let checkTarget: boolean = $page.url.searchParams.get('check_target') === 'true' || false
	let recursive: boolean = $page.url.searchParams.get('recursive') === 'true' || false
	let overwrite: boolean = $page.url.searchParams.get('overwrite') === 'true' || false
	let sortBySize: boolean = $page.url.searchParams.get('sort_by_size') === 'true' || false
	let ignoreErrors: boolean = $page.url.searchParams.get('ignore_errors') === 'true' || false
	let skipFiles: number = +($page.url.searchParams.get('minimum_size') || '0')
	let workerCount: number = +($page.url.searchParams.get('worker_count') || '5')

	const pipeline_id: string = $page.url.searchParams.get('pipeline_id') || ''
	let onCancelURL = $page.url.searchParams.get('from') || `/pipelines/${pipeline_id}`

	let starting: boolean = false
	const createProcess = async () => {
		if (starting) {
			return
		}
		starting = true

		if (equals(input.provider, 'File')) {
			const formData = new FormData()

			for (const file of files) {
				formData.append('file', file, file.name)
			}

			const fileUpload = await makeApiCall(Api.File, 'POST', formData, '', true)

			if (!fileUpload.ok) {
				const errorMessge = await fileUpload.text()
				toastStore.trigger(errorToast('File upload failed. ' + errorMessge + ' '))
				starting = false
				return
			}

			const json = await fileUpload.json()
			input.path = json.path
		}

		const response = await fetch('/process/api', {
			method: 'POST',
			body: JSON.stringify({
				pipeline_id: pipeline_id,
				input: { ...input },
				output: { ...output },
				settings: {
					notify: notify,
					check_target: checkTarget,
					recursive: recursive,
					overwrite: overwrite,
					sort_by_size: sortBySize,
					minimum_size: skipFiles || 0,
					worker_count: workerCount,
					ignore_errors: ignoreErrors
				}
			})
		})

		let process = await response.json()
		if (response.ok) {
			goto('/process/' + process.oid)
		} else {
			toastStore.trigger(errorToast(response.statusText))
			starting = false
		}
	}

	let inputBucketIsValid: string = ''
	let outputBucketIsValid: string = ''
	let settingsAreValid: string = ''

	$: {
		inputBucketIsValid = isValidS3BucketName(input.path)
		outputBucketIsValid = isValidS3BucketName(output.path)
		settingsAreValid = areSettingsValid(workerCount, skipFiles)
	}
</script>

<div class="h-full">
	<div class="grid">
		<div
			class="page-wrapper bg-solid md:top-0 z-10 left-0 bottom-0 right-0 row-start-2 fixed md:sticky md:row-start-1"
		>
			<div class="grid grid-cols-2 md:justify-between md:flex items-center gap-4 relative">
				<button class="button-primary" on:click={() => goto(onCancelURL)}>
					<Fa icon={faArrowLeft} />
					<span>Cancel</span>
				</button>
				<button
					class="button-success"
					disabled={starting || !isValidIO(input, output, files) || settingsAreValid.length !== 0}
					on:click={createProcess}
				>
					<Fa icon={faCheck} />
					<span>Submit</span>
				</button>
			</div>
		</div>
		<div class="p-4 md:p-8 space-y-4 container mx-auto">
			<h1 class="h1 mb-8">New Process</h1>
			<div
				class="grid {!equals(input.provider, IO.Text) && !equals(input.provider, IO.File)
					? 'md:grid-cols-2'
					: 'md:grid-cols-2'} gap-4"
			>
				<div
					class="section-wrapper p-4 md:p-8 space-y-8
				 {isValidInput(input, files) ? '!border-success-500 !border-2' : ''}"
				>
					<div class="flex items-center gap-4 justify-between">
						<h2 class="h2">Input</h2>
						{#if isValidInput(input, files)}
							<Fa icon={faCheck} class="text-success-500" size="2x" />
						{/if}
					</div>

					<div class="grid gap-4">
						<div class="flex items-center gap-4">
							<div class="flex-1">
								<Dropdown label="Source" options={InputSources} bind:value={input.provider} />
							</div>
							{#if !equals(input.provider, IO.Text)}
								<Dropdown
									label="File extension"
									name="input-extension"
									options={InputFileExtensions}
									bind:value={input.fileExtension}
								/>
							{/if}
						</div>

						{#if equals(input.provider, IO.Text)}
							<TextArea
								label="Document Text"
								name="content"
								error={input.content === '' ? 'Text cannot be empty' : ''}
								bind:value={input.content}
							/>
						{:else if equals(input.provider, IO.File)}
							<div class="space-y-1">
								<p class="form-label">File</p>
								<FileDropzone
									name="inputFile"
									bind:files
									accept={input.fileExtension}
									multiple={true}
									border="border border-color"
									rounded="rounded-md"
									class="input-wrapper"
								/>
								<p class="form-label">{files?.length || 0} files selected</p>
							</div>
						{:else if equals(input.provider, IO.Minio)}
							<TextInput
								label="Bucket name"
								error={inputBucketIsValid}
								name="inputPath"
								bind:value={input.path}
							/>
						{:else}
							<TextInput
								label="Path"
								error={input.path === ''
									? 'Path cannot be empty'
									: !input.path.startsWith('/') && equals(input.provider, Input.Dropbox)
									? 'Path should start with a /'
									: ''}
								name="inputPath"
								bind:value={input.path}
							/>
						{/if}
					</div>
				</div>

				<div
					class="section-wrapper p-4 md:p-8 space-y-8
				 {isValidOutput(output) ? '!border-success-500 !border-2' : ''}"
				>
					<div class="flex items-center gap-4 justify-between">
						<h2 class="h2">Output</h2>
						{#if isValidOutput(output)}
							<Fa icon={faCheck} class="text-success-500" size="2x" />
						{/if}
					</div>
					<div class="space-y-4">
						<div class="flex items-center gap-4">
							<div class="flex-1">
								<Dropdown label="Target" options={OutputTargets} bind:value={output.provider} />
							</div>
							{#if equals(output.provider, IO.Dropbox) || equals(output.provider, IO.Minio)}
								<Dropdown
									label="File extension"
									name="output-extension"
									options={OutputFileExtensions}
									bind:value={output.fileExtension}
								/>
							{/if}
						</div>

						{#if equals(output.provider, IO.Minio)}
							<TextInput
								label="Bucket name"
								error={outputBucketIsValid}
								name="output-folder"
								bind:value={output.path}
							/>
						{:else if equals(output.provider, IO.Dropbox)}
							<TextInput
								label="Folder"
								name="output-folder"
								error={output.path === ''
									? 'Path cannot be empty'
									: !output.path.startsWith('/') && equals(output.provider, IO.Dropbox)
									? 'Path should start with a /'
									: ''}
								bind:value={output.path}
							/>
						{/if}
					</div>
				</div>

				<div
					class="section-wrapper p-4 md:p-8 space-y-8 md:col-span-2
				{settingsAreValid.length === 0 ? '!border-success-500 !border-2' : ''}"
				>
					<div class="flex items-center gap-4 justify-between">
						<h2 class="h2">Settings</h2>
						{#if settingsAreValid.length === 0}
							<Fa icon={faCheck} class="text-success-500" size="2x" />
						{/if}
					</div>
					{#if settingsAreValid.length > 0}
						<p class="text-error-500">{settingsAreValid}</p>
					{/if}
					<div class="grid space-y-4">
						{#if !equals(input.provider, IO.Text)}
							<div class="grid grid-cols-2 gap-4">
								<div>
									<Number
										label="Skip files smaller than"
										max={2147483647}
										name="skipFiles"
										bind:value={skipFiles}
									/>
									<span class="text-xs pl-2">Bytes</span>
								</div>
								<Number
									label="Worker count"
									min={1}
									max={100}
									name="workerCount"
									bind:value={workerCount}
								/>
							</div>
						{/if}

						<div class="grid md:grid-cols-2 gap-4">
							{#if !equals(input.provider, IO.Text) && !equals(input.provider, IO.File)}
								<Checkbox
									bind:checked={recursive}
									name="recursive"
									label="Find files recursively starting in the path directory"
								/>
							{/if}

							{#if !equals(output.provider, Output.None)}
								<Checkbox
									bind:checked={checkTarget}
									name="checkTarget"
									label="Ignore files already present in the target location"
								/>
							{/if}
							{#if !equals(input.provider, IO.Text)}
								<Checkbox
									bind:checked={sortBySize}
									name="sortBySize"
									label="Sort files by size in ascending order"
								/>
							{/if}
							{#if equals(output.provider, IO.Dropbox)}
								<Checkbox
									bind:checked={overwrite}
									name="overwrite"
									label="Overwrite existing files on conflict"
								/>
							{/if}

							<Checkbox
								bind:checked={ignoreErrors}
								name="ignoreErrors"
								label="Ignore errors encountered by documents and skip to the next available one."
							/>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
</div>
