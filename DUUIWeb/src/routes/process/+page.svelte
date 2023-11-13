<script lang="ts">
	import { goto } from '$app/navigation'
	import { page } from '$app/stores'
	import CheckButton from '$lib/components/CheckButton.svelte'
	import { API_URL } from '$lib/config.js'
	import {
		Input,
		InputFileExtensions,
		InputSources,
		Output,
		OutputFileExtensions,
		OutputTargets,
		isCloudProvider,
		isValidIO,
		isValidInput,
		isValidOutput,
		isValidS3BucketName
	} from '$lib/duui/io.js'
	import { needsAuthorization } from '$lib/duui/user.js'
	import ActionButton from '$lib/svelte/widgets/action/ActionButton.svelte'
	import ComboBox from '$lib/svelte/widgets/input/ComboBox.svelte'
	import TextArea from '$lib/svelte/widgets/input/TextArea.svelte'
	import TextInput from '$lib/svelte/widgets/input/TextInput.svelte'
	import { equals } from '$lib/utils/text.js'
	import { faCancel, faCheck, faClose } from '@fortawesome/free-solid-svg-icons'
	import Fa from 'svelte-fa'

	export let data
	let { dbxAuthorized, session, dbxURL } = data

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

	const createProcess = async () => {
		let response = await fetch(API_URL + '/processes', {
			method: 'POST',
			mode: 'cors',
			headers: {
				session: session || ''
			},
			body: JSON.stringify({
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
		})

		let run = await response.json()
		if (response.ok) {
			goto('/process/' + run.oid)
		}
	}

	let inputBucketIsValid: string = ''
	let outputBucketIsValid: string = ''

	$: {
		inputBucketIsValid = isValidS3BucketName(input.folder)
		outputBucketIsValid = isValidS3BucketName(output.folder)
	}
</script>

<div class="container h-full flex-col mx-auto flex gap-4 md:my-8">
	<h1 class="h2">New Process</h1>
	<hr />
	<div class="items-center justify-between gap-4 hidden md:flex">
		<ActionButton
			tabindex={0}
			text="Cancel"
			icon={faCancel}
			variant="variant-soft-error"
			on:click={() => goto(onCancelURL)}
		/>
		<ActionButton
			text="Complete"
			icon={faCheck}
			variant="variant-soft-success"
			on:click={createProcess}
			leftToRight={false}
			_class={isValidIO(input, output) ? 'visible' : 'invisible'}
		/>
	</div>
	<div class="grid md:grid-cols-2 gap-4">
		<div
			class="variant-soft-surface p-4 shadow-lg space-y-4 {isValidInput(input)
				? ''
				: 'border-[1px] border-error-400'}"
		>
			<h2 class="h3">Input</h2>
			<div class="grid grid-cols-2 gap-4">
				<label class="label space-y-2 flex flex-col" for="source">
					<span>Source</span>
					<ComboBox id="source" options={InputSources} bind:value={inputSource} />
				</label>
				<label class="label space-y-2 flex flex-col" for="input-extension">
					<span>File extension</span>
					<ComboBox
						id="input-extension"
						options={InputFileExtensions}
						bind:value={inputFileExtension}
					/>
				</label>
			</div>

			{#if equals(inputSource, 'Text')}
				<label class="col-span-2 label space-y-2" for="content">
					<span>Document Text</span>
					<TextArea id="content" bind:value={inputContent} rows={2} />
				</label>
			{:else if equals(input.source, 'minio')}
				<label class="col-span-2 label space-y-2" for="input-folder">
					<span class={inputBucketIsValid.length > 0 ? 'text-error-400' : ''}
						>Bucket name {inputBucketIsValid}</span
					>
					<TextInput id="input-folder" bind:value={inputFolder} />
				</label>
			{:else}
				<label class="col-span-2 label space-y-2" for="input-folder">
					<span>Path to folder</span>
					<TextInput id="input-folder" bind:value={inputFolder} />
				</label>
			{/if}
		</div>

		<div
			class="variant-soft-surface p-4 shadow-lg space-y-4 {isValidOutput(output)
				? ''
				: 'border-[1px] border-error-400'}"
		>
			<h2 class="h3 col-span-2">Output</h2>
			<div class="grid grid-cols-2 gap-4">
				<label class="label space-y-2 flex flex-col" for="target">
					<span>Target</span>
					<ComboBox id="target" options={OutputTargets} bind:value={outputTarget} />
				</label>
				<label class="label space-y-2 flex flex-col" for="output-extension">
					<span>File extension</span>
					<ComboBox
						id="output-extension"
						options={OutputFileExtensions}
						bind:value={outputFileExtension}
					/>
				</label>
			</div>
			{#if equals(output.target, 'minio')}
				<label class="label space-y-2" for="output-folder">
					<span class={outputBucketIsValid.length > 0 ? 'text-error-400' : ''}
						>Bucket name {outputBucketIsValid}</span
					>
					<TextInput id="output-folder" bind:value={outputFolder} />
				</label>
			{:else if equals(output.target, 'dropbox')}
				<label class="label space-y-2" for="output-folder">
					<span>Path to folder</span>
					<TextInput id="output-folder" bind:value={outputFolder} />
				</label>
			{/if}
		</div>

		{#if !dbxAuthorized && needsAuthorization(inputSource, outputTarget)}
			<a href={dbxURL.toString()} target="_blank" class="btn variant-filled-primary">
				Authenticate Dropbox
			</a>
		{/if}
	</div>
	<div class="grid md:grid-cols-3 lg:grid-cols-4 gap-4">
		<CheckButton text="Enable Notifications" bind:checked={notify} />

		{#if isCloudProvider(outputTarget)}
			<CheckButton text="Check Target for Documents" bind:checked={checkTarget} />
		{/if}

		{#if isCloudProvider(input.source) || isCloudProvider(output.target)}
			<CheckButton text="Find Documents recursively" bind:checked={recursive} />
		{/if}
	</div>
	<div class="items-center justify-between gap-4 flex md:hidden">
		<ActionButton
			tabindex={0}
			text="Cancel"
			icon={faCancel}
			variant="variant-soft-error"
			on:click={() => goto(onCancelURL)}
		/>
		<ActionButton
			text="Complete"
			icon={faCheck}
			variant="variant-soft-success"
			on:click={createProcess}
			leftToRight={false}
		/>
	</div>

	<!-- <Stepper
		on:complete={submitProcess}
		buttonCompleteLabel="Start"
		class="max-w-5xl mx-auto p-4"
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
									<span class={!inputBucketIsValid ? 'text-error-400' : ''}>Bucket Name</span>
									<div class="[&>*]:pointer-events-none" use:popup={s3NamingPopup}>
										<Fa icon={faQuestionCircle} />
									</div>
								</div>
								<input class="input border-2" type="text" bind:value={inputFolder} />
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
					{#if isCloudProvider(outputTarget)}
						{#if equals(output.target, 'minio')}
							<label class="label space-y-2">
								<div class="flex items-center gap-2">
									<span class={!outputBucketIsValid ? 'text-error-400' : ''}>Bucket Name</span>
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
				<ActionButton
					text="Cancel"
					variant="variant-soft-error"
					icon={faCancel}
					on:click={() => goto(onCancelURL)}
				/>
			</svelte:fragment>
		</Step>
		<Step locked={!dbxAuthorized && needsAuthorization(inputSource, outputTarget)}>
			<svelte:fragment slot="header">Settings</svelte:fragment>
			
		</Step>
	</Stepper> -->
</div>
