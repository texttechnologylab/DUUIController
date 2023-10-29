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
		isValidIO,
		isValidS3BucketName,
		isCloudProvider
	} from '$lib/duui/io.js'
	import { needsAuthorization } from '$lib/duui/user.js'
	import { equals } from '$lib/utils/text.js'
	import { faQuestionCircle } from '@fortawesome/free-solid-svg-icons'

	import { Step, Stepper, type PopupSettings, popup } from '@skeletonlabs/skeleton'
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
				},
				settings: {
					notify: notify,
					checkTarget: checkTarget,
					recursive: recursive
				}
			})
		})

		let content = await response.json()
		if (response.ok) {
			goto('/process/' + content.id)
		}
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
			<button
				class="btn variant-filled-error"
				on:click={() => goto('/pipelines/' + $page.url.searchParams.get('pipeline'))}>Cancel</button
			>
		</svelte:fragment>
	</Step>
	<Step locked={!dbxAuthorized && needsAuthorization(inputSource, outputTarget)}>
		<svelte:fragment slot="header">Settings</svelte:fragment>
		<div class="grid md:grid-cols-3 gap-4">
			<CheckButton text="Enable Notifications" bind:checked={notify} />

			{#if isCloudProvider(outputTarget)}
				<CheckButton text="Check Target for Documents" bind:checked={checkTarget} />
			{/if}

			{#if isCloudProvider(input.source) || isCloudProvider(output.target)}
				<CheckButton text="Find Documents recursively" bind:checked={recursive} />
			{/if}
		</div>

		{#if !dbxAuthorized && needsAuthorization(inputSource, outputTarget)}
			<a href={dbxURL.toString()} target="_blank" class="btn variant-filled-primary">
				Authenticate Dropbox
			</a>
		{/if}
	</Step>
</Stepper>
