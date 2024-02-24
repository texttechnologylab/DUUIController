<script lang="ts">
	import { goto } from '$app/navigation'
	import { page } from '$app/stores'
	import {
		INPUT_EXTENSIONS,
		IO,
		IO_INPUT,
		IO_OUTPUT,
		OUTPUT_EXTENSIONS,
		areSettingsValid,
		isValidIO,
		isValidInput,
		isValidOutput,
		isValidS3BucketName,
		type DUUIDocumentProvider,
		type FileExtension,
		type IOProvider,
		isValidFileUpload
	} from '$lib/duui/io.js'
	import { Languages } from '$lib/duui/process'
	import { equals } from '$lib/duui/utils/text'
	import { errorToast } from '$lib/duui/utils/ui'
	import Checkbox from '$lib/svelte/components/Input/Checkbox.svelte'
	import Dropdown from '$lib/svelte/components/Input/Dropdown.svelte'
	import Number from '$lib/svelte/components/Input/Number.svelte'
	import TextArea from '$lib/svelte/components/Input/TextArea.svelte'
	import TextInput from '$lib/svelte/components/Input/TextInput.svelte'
	import {
		faArrowLeft,
		faCheck,
		faCloud,
		faCloudUpload,
		faFileArrowUp
	} from '@fortawesome/free-solid-svg-icons'
	import { FileDropzone, getToastStore, ProgressBar } from '@skeletonlabs/skeleton'
	import Fa from 'svelte-fa'

	const toastStore = getToastStore()

	let input: DUUIDocumentProvider = {
		provider: ($page.url.searchParams.get('input_provider') as IOProvider) || 'Text',
		path: $page.url.searchParams.get('input_path') || '',
		content: $page.url.searchParams.get('input_content') || 'Sample Text.',
		file_extension: ($page.url.searchParams.get('input_file_extension') as FileExtension) || '.txt'
	}

	if (input.provider === IO.File) {
		input.path = ''
	}

	let output: DUUIDocumentProvider = {
		provider: ($page.url.searchParams.get('output_provider') as IOProvider) || 'None',
		path: $page.url.searchParams.get('output_path') || '',
		content: $page.url.searchParams.get('output_content') || '',
		file_extension: ($page.url.searchParams.get('output_file_extension') as FileExtension) || '.txt'
	}

	let files: FileList

	let language: string = $page.url.searchParams.get('language') || 'Unspecified'
	let notify: boolean = $page.url.searchParams.get('notify') === 'true' || false
	let checkTarget: boolean = $page.url.searchParams.get('check_target') === 'true' || false
	let recursive: boolean = $page.url.searchParams.get('recursive') === 'true' || true
	let overwrite: boolean = $page.url.searchParams.get('overwrite') === 'true' || false
	let sortBySize: boolean = $page.url.searchParams.get('sort_by_size') === 'true' || false
	let ignoreErrors: boolean = $page.url.searchParams.get('ignore_errors') === 'true' || true
	let skipFiles: number = +($page.url.searchParams.get('minimum_size') || '0')
	let workerCount: number = +($page.url.searchParams.get('worker_count') || '1')

	const pipeline_id: string = $page.url.searchParams.get('pipeline_id') || ''
	let onCancelURL = $page.url.searchParams.get('from') || `/pipelines/${pipeline_id}`

	let starting: boolean = false
	let uploading: boolean = false

	let fileStorage = {
		storeFiles: false,
		provider: IO.Dropbox,
		path: '/upload'
	}

	const uploadFiles = async () => {
		if (!files) return false

		uploading = true
		const formData = new FormData()

		for (const file of files) {
			formData.append('file', file, file.name)
		}

		const fileUpload = await fetch(
			`/api/files/upload?store=${fileStorage.storeFiles}&provider=${fileStorage.provider}&path=${fileStorage.path}`,
			{
				method: 'Post',
				body: formData
			}
		)

		if (!fileUpload.ok) {
			const errorMessge = await fileUpload.text()
			toastStore.trigger(errorToast('File upload failed. ' + errorMessge + ' '))
			uploading = false
			return false
		}

		const json = await fileUpload.json()
		input.path = json.path

		uploading = false
		return true
	}

	const createProcess = async () => {
		if (starting) {
			return
		}

		starting = true

		if (equals(input.provider, 'File')) {
			const result = await uploadFiles()
			if (!result) {
				starting = false
				uploading = false
				return
			}
		}

		if (
			!input.path.startsWith('/') &&
			equals(input.provider, IO.Dropbox) &&
			input.path.length > 0
		) {
			input.path = '/' + input.path
		}

		if (
			!output.path.startsWith('/') &&
			equals(output.provider, IO.Dropbox) &&
			output.path.length > 0
		) {
			output.path = '/' + output.path
		}

		if (input.provider !== IO.Text) {
			input.content = ''
		}

		const response = await fetch('api/processes', {
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
					ignore_errors: ignoreErrors,
					language: language
				}
			})
		})

		let process = await response.json()
		if (response.ok) {
			goto('/processes/' + process.oid)
		} else {
			toastStore.trigger(errorToast(response.statusText))
			starting = false
		}
	}

	let inputBucketIsValid: string = ''
	let outputBucketIsValid: string = ''
	let settingsAreValid: string = ''
	let isValidFileStorage: boolean = false

	$: {
		inputBucketIsValid = isValidS3BucketName(input.path)
		outputBucketIsValid = isValidS3BucketName(output.path)
		settingsAreValid = areSettingsValid(workerCount, skipFiles)
		isValidFileStorage =
			input.provider !== IO.File || !fileStorage.storeFiles || isValidFileUpload(fileStorage)
	}
</script>

<div class="menu-mobile">
	<button class="button-mobile" on:click={() => goto(onCancelURL)}>
		<Fa icon={faArrowLeft} />
		<span>Cancel</span>
	</button>
	<button
		class="button-mobile"
		disabled={starting ||
			!isValidFileStorage ||
			!isValidIO(input, output, files) ||
			settingsAreValid.length !== 0}
		on:click={createProcess}
	>
		<Fa icon={faCheck} />
		<span>Submit</span>
	</button>
</div>

<div class="h-full">
	<div class="sticky top-0 bg-surface-50-900-token border-b border-color hidden md:block z-10">
		<div class="grid grid-cols-2 md:justify-between md:flex items-center relative">
			<button class="button-menu border-r border-color" on:click={() => goto(onCancelURL)}>
				<Fa icon={faArrowLeft} />
				<span>Cancel</span>
			</button>
			<button
				class="button-menu border-l border-color"
				disabled={starting ||
					!isValidFileStorage ||
					!isValidIO(input, output, files) ||
					settingsAreValid.length !== 0}
				on:click={createProcess}
			>
				<Fa icon={faCheck} />
				<span>Submit</span>
			</button>
		</div>
	</div>
	<div class="p-4 md:p-16 space-y-8 pb-16">
		{#if uploading}
			<div class="w-full">
				<div class="section-wrapper p-8 space-y-8">
					<div class="flex-center-4 justify-between">
						<p>Uploading {files?.length || 0} files</p>

						<Fa icon={faFileArrowUp} size="3x" class="text-surface-300/20" />
					</div>
					<ProgressBar
						value={undefined}
						height="h-4"
						rounded="rounded-full"
						track="bg-surface-100-800-token"
						meter="variant-filled-primary"
						class="bordered-soft"
					/>
					<a href={`/pipelines/${pipeline_id}?tab=1`} class="anchor-neutral">
						<Fa icon={faArrowLeft} />
						<span>Pipeline</span>
					</a>
				</div>
			</div>
		{:else}
			<div class="container mx-auto max-w-4xl grid gap-4">
				<div class="grid gap-4">
					<div
						class="section-wrapper space-y-4 p-4
				 {isValidInput(input, files) ? '!border-success-500 ' : '!border-error-500'}"
					>
						<!-- INPUT -->
						<div class="flex-center-4 justify-between">
							<h2 class="h2">Input</h2>
							{#if isValidInput(input, files)}
								<Fa icon={faCheck} class="text-success-500" size="2x" />
							{/if}
						</div>

						<div class="grid gap-4">
							<div class="flex-center-4">
								<div class="flex-1">
									<Dropdown label="Source" options={IO_INPUT} bind:value={input.provider} />
								</div>
								{#if !equals(input.provider, IO.Text)}
									<Dropdown
										label="File extension"
										name="input-extension"
										options={INPUT_EXTENSIONS}
										bind:value={input.file_extension}
									/>
								{/if}
							</div>

							<Dropdown label="Language" options={Languages} bind:value={language} />

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
										accept={input.file_extension}
										multiple={true}
										border="border border-color"
										rounded="rounded-md"
										class="input-wrapper"
									/>
									<p class="form-label {(files?.length || 0) === 0 ? 'text-error-500' : ''}">
										{files?.length || 0} files selected
									</p>
								</div>

								<Checkbox
									label="Upload input files to cloud storage."
									bind:checked={fileStorage.storeFiles}
								/>
								{#if fileStorage.storeFiles}
									<div class="flex-1">
										<Dropdown
											label="Provider"
											options={[IO.Dropbox, IO.Minio]}
											bind:value={fileStorage.provider}
										/>
									</div>
									{#if equals(fileStorage.provider, IO.Minio)}
										<TextInput
											label="Path (bucket/path/to/file)"
											name="fileStoragePath"
											bind:value={fileStorage.path}
											error={inputBucketIsValid}
										/>
									{:else}
										<TextInput
											label="Path"
											name="fileStoragePath"
											bind:value={fileStorage.path}
											error={['/', ''].includes(fileStorage.path)
												? 'Writing to the root folder is not possible.'
												: ''}
										/>
									{/if}
								{/if}
							{:else if equals(input.provider, IO.Minio)}
								<TextInput
									label="Path (bucket/path/to/file)"
									error={inputBucketIsValid}
									name="inputPath"
									bind:value={input.path}
								/>
							{:else}
								<TextInput
									label="Path"
									name="inputPath"
									bind:value={input.path}
									error={input.path === '/'
										? 'Provide an empty path to select the root folder.'
										: ''}
								/>
							{/if}
						</div>

						<hr class="hr !w-full" />
						<div class="space-y-4">
							<h3 class="h3">Settings</h3>
							<div class="space-y-4">
								{#if !equals(input.provider, IO.Text)}
									<div class="grid grid-cols-2 gap-4 items-start">
										<div>
											<Number
												label="Minimum size"
												max={2147483647}
												name="skipFiles"
												help="All files with a size smaller than {skipFiles} bytes will not be processed."
												bind:value={skipFiles}
											/>
											<span class="text-xs pl-2">Bytes</span>
										</div>
										<Number
											label="Worker count"
											min={1}
											max={100}
											help="The number of threads used for processing. The actual number of threads is limited by the system."
											name="workerCount"
											bind:value={workerCount}
										/>
									</div>
								{/if}
								{#if !equals(input.provider, IO.Text) && !equals(input.provider, IO.File)}
									<Checkbox
										bind:checked={recursive}
										name="recursive"
										label="Find files recursively starting in the path directory"
									/>
								{/if}
								{#if !equals(input.provider, IO.Text)}
									<Checkbox
										bind:checked={sortBySize}
										name="sortBySize"
										label="Sort files by size in ascending order"
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

					<div
						class="section-wrapper p-4 space-y-4 flex flex-col justify-start relative
				 {isValidOutput(output) ? '!border-success-500 ' : '!border-error-500'}"
					>
						<div class="flex-center-4 justify-between">
							<h2 class="h2">Output</h2>
							{#if isValidOutput(output)}
								<Fa icon={faCheck} class="text-success-500" size="2x" />
							{/if}
						</div>
						<div class="space-y-4">
							<div class="flex-center-4">
								<div class="flex-1">
									<Dropdown label="Target" options={IO_OUTPUT} bind:value={output.provider} />
								</div>
								{#if equals(output.provider, IO.Dropbox) || equals(output.provider, IO.Minio)}
									<Dropdown
										label="File extension"
										name="output-extension"
										options={OUTPUT_EXTENSIONS}
										bind:value={output.file_extension}
									/>
								{/if}
							</div>
							{#if equals(output.provider, IO.Minio)}
								<TextInput
									label="Path (bucket/path/to/file)"
									error={outputBucketIsValid}
									name="output-folder"
									bind:value={output.path}
								/>
							{:else if equals(output.provider, IO.Dropbox)}
								<TextInput
									label="Path"
									name="output-folder"
									error={['/', ''].includes(output.path)
										? 'Writing to the root folder is not possible.'
										: ''}
									bind:value={output.path}
								/>
							{/if}
							{#if output.provider !== IO.None}
								<hr class="hr !w-full" />
								<div class="space-y-4">
									<h3 class="h3">Settings</h3>
									<div class="space-y-4">
										{#if !equals(output.provider, IO.None)}
											<Checkbox
												bind:checked={checkTarget}
												name="checkTarget"
												label="Ignore files already present in the target location"
											/>
										{/if}

										{#if equals(output.provider, IO.Dropbox)}
											<Checkbox
												bind:checked={overwrite}
												name="overwrite"
												label="Overwrite existing files on conflict"
											/>
										{/if}
									</div>
								</div>
							{/if}
						</div>
						{#if output.provider === IO.None}
							<div class="grow flex items-center justify-center flex-col gap-4 p-4">
								<div class="relative opacity-50">
									<Fa icon={faCloudUpload} size="8x" class="text-primary-500" />
									<Fa
										icon={faCloud}
										size="8x"
										class="top-1/2 left-1/2 -translate-x-[60%] -translate-y-[60%] absolute text-primary-500/50"
									/>
								</div>
								<p class="font-bold">Select an output location above</p>
							</div>
						{/if}
					</div>
				</div>
			</div>
		{/if}
	</div>
</div>
