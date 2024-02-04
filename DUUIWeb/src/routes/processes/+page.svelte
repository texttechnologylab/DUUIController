<script lang="ts">
	import { goto } from '$app/navigation'
	import { page } from '$app/stores'
	import {
		IO,
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
		type IOProvider
	} from '$lib/duui/io.js'
	import { equals } from '$lib/duui/utils/text'
	import { errorToast } from '$lib/duui/utils/ui'
	import Checkbox from '$lib/svelte/components/Checkbox.svelte'
	import Dropdown from '$lib/svelte/components/Dropdown.svelte'
	import Number from '$lib/svelte/components/Number.svelte'
	import TextArea from '$lib/svelte/components/TextArea.svelte'
	import TextInput from '$lib/svelte/components/TextInput.svelte'
	import { faArrowLeft, faCheck } from '@fortawesome/free-solid-svg-icons'
	import { FileDropzone, getToastStore } from '@skeletonlabs/skeleton'
	import Fa from 'svelte-fa'

	const toastStore = getToastStore()

	let input: DUUIDocumentProvider = {
		provider: ($page.url.searchParams.get('input-provider') as IOProvider) || 'Text',
		path: $page.url.searchParams.get('input-path') || '',
		content: $page.url.searchParams.get('input-content') || 'Sample Text.',
		file_extension: ($page.url.searchParams.get('input-file-extension') as FileExtension) || '.txt'
	}

	let output: DUUIDocumentProvider = {
		provider: ($page.url.searchParams.get('output-provider') as IOProvider) || 'None',
		path: $page.url.searchParams.get('output-path') || '',
		content: $page.url.searchParams.get('output-content') || '',
		file_extension: ($page.url.searchParams.get('output-file-extension') as FileExtension) || '.txt'
	}

	let files: FileList

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

			const fileUpload = await fetch('/api/files/upload', {
				method: 'Post',
				body: formData
			})

			if (!fileUpload.ok) {
				const errorMessge = await fileUpload.text()
				toastStore.trigger(errorToast('File upload failed. ' + errorMessge + ' '))
				starting = false
				return
			}

			const json = await fileUpload.json()
			input.path = json.path
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
					ignore_errors: ignoreErrors
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

	$: {
		inputBucketIsValid = isValidS3BucketName(input.path)
		outputBucketIsValid = isValidS3BucketName(output.path)
		settingsAreValid = areSettingsValid(workerCount, skipFiles)
	}
</script>

<div class="menu-mobile">
	<button class="button-mobile" on:click={() => goto(onCancelURL)}>
		<Fa icon={faArrowLeft} />
		<span>Cancel</span>
	</button>
	<button
		class="button-mobile"
		disabled={starting || !isValidIO(input, output, files) || settingsAreValid.length !== 0}
		on:click={createProcess}
	>
		<Fa icon={faCheck} />
		<span>Submit</span>
	</button>
</div>

<div class="h-full pb-16">
	<div class="sticky top-0 bg-surface-50-900-token border-y p-4 border-color hidden md:block z-10">
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
	<div class="p-4 space-y-4 container mx-auto">
		<div class="grid md:grid-cols-2 gap-4">
			<div
				class="section-wrapper space-y-4 p-4
				 {isValidInput(input, files) ? '!border-success-500 ' : '!border-error-500'}"
			>
				<!-- INPUT -->
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
								bind:value={input.file_extension}
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
								accept={input.file_extension}
								multiple={true}
								border="border border-color"
								rounded="rounded-md"
								class="input-wrapper"
							/>
							<p class="form-label">{files?.length || 0} files selected</p>
						</div>
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
							error={input.path === '/' ? 'Provide an empty path to select the root folder.' : ''}
						/>
					{/if}
				</div>

				<hr class="hr !w-full" />
				<div class="space-y-4">
					<h3 class="h3">Settings</h3>
					<div class="space-y-4">
						{#if !equals(input.provider, IO.Text)}
							<div class="grid grid-cols-2 gap-4 items-start md:items-center">
								<div>
									<Number
										label="Minimum size"
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
				class="section-wrapper p-4 space-y-4 flex flex-col justify-start
				 {isValidOutput(output) ? '!border-success-500 ' : '!border-error-500'}"
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
							error={output.path === '/' ? 'Provide an empty path to select the root folder.' : ''}
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
			</div>
		</div>
	</div>
</div>
