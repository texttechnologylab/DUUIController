<script lang="ts">
	import {
		INPUT_EXTENSIONS,
		IO,
		IO_INPUT,
		isValidS3BucketName,
		type DUUIDocumentProvider
	} from '$lib/duui/io'
	import { Languages } from '$lib/duui/process'
	import { equals } from '$lib/duui/utils/text'
	import Dropdown from '$lib/svelte/components/Input/Dropdown.svelte'
	import TextArea from '$lib/svelte/components/Input/TextArea.svelte'
	import TextInput from '$lib/svelte/components/Input/TextInput.svelte'
	import { FileDropzone } from '@skeletonlabs/skeleton'
	import Node from '$lib/svelte/components/Node.svelte'

	export let input: DUUIDocumentProvider
	export let language: string = 'Unspecified'
	export let files: FileList

	$: inputBucketIsValid = isValidS3BucketName(input.path)


</script>

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
