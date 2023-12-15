<script lang="ts">
	import { goto } from '$app/navigation'
	import { page } from '$app/stores'
	import {
		Input,
		InputFileExtensions,
		InputSources,
		Output,
		OutputFileExtensions,
		OutputTargets,
		isCloudProvider,
		isValidIO,
		isValidS3BucketName
	} from '$lib/duui/io.js'
	import ActionButton from '$lib/svelte/widgets/action/ActionButton.svelte'
	import TextArea from '$lib/svelte/widgets/input/TextArea.svelte'
	import { equals } from '$lib/utils/text.js'
	import { faArrowLeft, faCancel, faCheck, faLink } from '@fortawesome/free-solid-svg-icons'
	import Dropdown from '$lib/svelte/widgets/input/Dropdown.svelte'
	import Text from '$lib/svelte/widgets/input/Text.svelte'
	import Checkbox from '$lib/svelte/widgets/input/Checkbox.svelte'
	import { Api, makeApiCall } from '$lib/utils/api.js'
	import { fly } from 'svelte/transition'
	import Fa from 'svelte-fa'

	export let data
	let { user } = data

	let inputSource: string = $page.url.searchParams.get('input-source') || 'Text'
	let inputFolder: string = $page.url.searchParams.get('input-folder') || '/input/sample_txt'
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
	let checkTarget: boolean = false
	let recursive: boolean = false

	let onCancelURL =
		$page.url.searchParams.get('from') ||
		`/pipelines/${$page.url.searchParams.get('oid') || ''}?tab=2`

	let starting: boolean = false
	const createProcess = async () => {
		if (starting) {
			return
		}
		starting = true
		const response = await makeApiCall(
			Api.Processes,
			'POST',
			JSON.stringify({
				pipeline_id: $page.url.searchParams.get('oid'),
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
				},
				settings: {
					notify: notify,
					checkTarget: checkTarget,
					recursive: recursive
				}
			})
		)

		let process = await response.json()
		if (response.ok) {
			goto('/process/' + process.oid)
		}
	}

	let inputBucketIsValid: string = ''
	let outputBucketIsValid: string = ''

	$: {
		inputBucketIsValid = isValidS3BucketName(input.folder)
		outputBucketIsValid = isValidS3BucketName(output.folder)
	}

	let needsDropboxAuthentication: boolean = false
	let needsMinioAuthentication: boolean = false
	let needsAuthentication: boolean = needsDropboxAuthentication || needsMinioAuthentication
	$: {
		needsDropboxAuthentication =
			(!user.connections.dropbox && equals(inputSource, Input.Dropbox)) ||
			(!user.connections.dropbox && equals(outputTarget, Output.Dropbox))

		needsMinioAuthentication =
			(!user.connections.minio && equals(inputSource, Input.Minio)) ||
			(!user.connections.minio && equals(outputTarget, Output.Minio))

		needsAuthentication = needsDropboxAuthentication || needsMinioAuthentication
	}
</script>

<div class="container h-full flex-col mx-auto flex gap-4 md:my-8">
	<h1 class="h2">New Process</h1>
	<hr class="bg-surface-400/20 h-[1px] !border-0 rounded" />
	<div class="items-center justify-between gap-4 hidden md:flex">
		<ActionButton
			tabindex={0}
			text="Cancel"
			icon={faCancel}
			variant="dark:variant-soft-error variant-filled-error"
			on:click={() => goto(onCancelURL)}
		/>
		<ActionButton
			text="Start"
			icon={faCheck}
			variant="dark:variant-soft-success variant-filled-success"
			on:click={createProcess}
			leftToRight={false}
			disabled={needsAuthentication || starting || !isValidIO(input, output)}
		/>
	</div>
	<div
		class="bg-surface-100 dark:variant-soft-surface p-4 shadow-lg grid md:grid-cols-3 lg:grid-cols-4 gap-4"
	>
		<Checkbox label="Enable Notifications" name="notification" bind:checked={notify} />

		{#if isCloudProvider(input.source) || isCloudProvider(output.target)}
			<Checkbox label="Find Documents recursively" name="recursive" bind:checked={recursive} />
		{/if}

		{#if isCloudProvider(output.target)}
			<Checkbox label="Check Target for Documents" name="check-target" bind:checked={checkTarget} />
		{/if}
	</div>
	<div class="grid md:grid-cols-2 gap-4">
		<div class="bg-surface-100 dark:variant-soft-surface p-4 shadow-lg space-y-4">
			<h2 class="h3">Input</h2>
			<div class="grid grid-cols-2 gap-4">
				<Dropdown label="Source" options={InputSources} bind:value={inputSource} />
				<Dropdown
					label="File extension"
					name="input-extension"
					options={InputFileExtensions}
					bind:value={inputFileExtension}
				/>
			</div>

			{#if equals(inputSource, 'Text')}
				<TextArea
					label="Document Text"
					name="content"
					error={inputContent === '' ? 'Text cannot be empty' : ''}
					bind:value={inputContent}
				/>
			{:else if equals(input.source, 'minio')}
				<Text
					label="Bucket name"
					error={inputBucketIsValid}
					name="Bucket name"
					bind:value={inputFolder}
				/>
			{:else}
				<Text
					label="Folder"
					error={inputFolder === ''
						? 'Folder cannot be empty'
						: !inputFolder.startsWith('/')
						? 'Path should start with a /'
						: ''}
					name="Bucket name"
					bind:value={inputFolder}
				/>
			{/if}
		</div>

		<div class="bg-surface-100 dark:variant-soft-surface p-4 shadow-lg space-y-4">
			<h2 class="h3 col-span-2">Output</h2>
			<div class="grid grid-cols-2 gap-4">
				<Dropdown label="Target" options={OutputTargets} bind:value={outputTarget} />
				<Dropdown
					label="File extension"
					name="output-extension"
					options={OutputFileExtensions}
					bind:value={outputFileExtension}
				/>
			</div>
			{#if equals(output.target, 'minio')}
				<Text
					label="Bucket name"
					error={outputBucketIsValid}
					name="output-folder"
					bind:value={outputFolder}
				/>
			{:else if equals(output.target, 'dropbox')}
				<Text
					label="Folder"
					name="output-folder"
					error={outputFolder === '' ? 'Folder cannot be empty' : ''}
					bind:value={outputFolder}
				/>
			{/if}
		</div>
	</div>
	{#if needsAuthentication}
		<div
			in:fly
			class="bg-surface-100 dark:variant-soft-surface p-4 shadow-lg flex justify-center gap-4"
		>
			{#if needsDropboxAuthentication}
				<a
					href="/account/user/connections"
					target="_blank"
					class="btn rounded-none variant-ringed-primary"
				>
					<Fa icon={faLink} />
					<span>Connect Dropbox</span>
				</a>
			{/if}
			{#if needsMinioAuthentication}
				<ActionButton text="Connect Minio" icon={faLink} />
			{/if}
		</div>
	{/if}

	<div class="items-center justify-between gap-4 flex md:hidden">
		<ActionButton
			tabindex={0}
			text="Cancel"
			icon={faCancel}
			variant="variant-soft-error"
			on:click={() => goto(onCancelURL)}
		/>
		<ActionButton
			text="Start"
			icon={faCheck}
			variant="variant-villed-success dark:variant-soft-success"
			on:click={createProcess}
			leftToRight={false}
		/>
	</div>
</div>
