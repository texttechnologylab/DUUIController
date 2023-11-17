<script lang="ts">
	import ActionButton from '$lib/svelte/widgets/action/ActionButton.svelte'
	import { makeApiCall, Api } from '$lib/utils/api.js'
	import {
		faCopy,
		faCheck,
		faRefresh,
		faAdd,
		faTrash,
		faFilePen,
		faFileText,
		faLink
	} from '@fortawesome/free-solid-svg-icons'
	import { clipboard } from '@skeletonlabs/skeleton'
	import Fa from 'svelte-fa'
	import Password from '$lib/svelte/widgets/input/Password.svelte'
	import { goto } from '$app/navigation'
	import { localStorageStore } from '@skeletonlabs/skeleton'
	import type { Writable } from 'svelte/store'
	import { storage } from '$lib/store.js'

	export let data
	const { user, session, dropbBoxURL } = data

	let access: string = 'Cedric-Test-App'

	const generateApiKey = async () => {
		const response = await makeApiCall(Api.Authentication, 'GET')
		if (response.ok) {
			const data = await response.json()
			user.authorization = data.authorization
		}
	}

	const deleteApiKey = async () => {
		const response = await makeApiCall(Api.Authentication, 'DELETE')
		if (response.ok) {
			user.authorization = ''
		}
	}

	const connectDropbox = async () => {
		$storage = session
		goto(dropbBoxURL.toString())
		const response = await makeApiCall(Api.Dropbox, 'POST', {})
		console.log(await response.json())
	}

	const revokeDropboxAccess = async () => {
		const response = await makeApiCall(Api.Dropbox, 'DELETE', {})

		if (response.ok) {
			user.connections.dropbox = false
		}
	}

	const connectMinio = () => {}

	const revokeMinioAccess = () => {}

	let success: boolean = false
</script>

<section class="space-y-8">
	<div
		class="space-y-8 border-l-8 pl-4 {user.authorization
			? 'border-success-500/50'
			: 'border-error-500/50'}"
	>
		<h3 class="h3">Api key</h3>
		<div class="md:pl-8">
			{#if user.authorization}
				<div class="flex flex-col-reverse md:flex-row items-start md:items-center gap-4">
					<button class="btn rounded-none variant-soft-primary" use:clipboard={user.authorization}>
						<Fa icon={faCopy} />
						<span>Copy Key</span>
					</button>
					<p>Don't share this key, anyone with this key can make api calls in your name.</p>
				</div>
				<Password style="grow mt-4" readonly={true} bind:value={user.authorization} />
			{/if}
			<div class="mt-4 flex gap-4">
				<ActionButton
					text={user.authorization ? 'Regenerate' : 'Generate API key'}
					icon={user.authorization ? faRefresh : faAdd}
					on:click={generateApiKey}
				/>
				{#if user.authorization}
					<ActionButton
						text="Delete key"
						icon={faTrash}
						variant="variant-filled-error dark:variant-soft-error"
						on:click={deleteApiKey}
					/>
				{/if}
			</div>
		</div>
	</div>

	<hr class="bg-surface-400/20 h-[1px] !border-0 rounded" />

	<div
		class="space-y-8 border-l-8 pl-4 {user.connections.dropbox
			? 'border-success-500/50'
			: 'border-error-500/50'}"
	>
		<h3 class="h3">Dropbox</h3>
		<div class="md:pl-8">
			{#if user.connections.dropbox}
				<p class="flex items-center gap-4">
					<Fa icon={faCheck} />
					<span>Read files and folders contained in the <strong>{access}</strong> folder</span>
				</p>
				<p class="flex items-center gap-4 mb-4">
					<Fa icon={faCheck} />
					<span>Create files and folders in the <strong>{access}</strong> folder</span>
				</p>
				<ActionButton
					icon={faTrash}
					text="Revoke access"
					variant="variant-filled-error dark:variant-soft-error"
					on:click={revokeDropboxAccess}
				/>
			{:else}
				<p class="mb-8">
					By connecting Dropbox and DUUI you can directly read and write data from and to your
					Dropbox storage.
				</p>
				<p class="flex items-center gap-[22px]">
					<Fa icon={faFileText} />
					<span>Read files and folders contained in the <strong>{access}</strong> folder</span>
				</p>
				<p class="flex items-center gap-4 mb-4">
					<Fa icon={faFilePen} />
					<span>Create files and folders in the <strong>{access}</strong> folder</span>
				</p>
				<ActionButton icon={faLink} text="Connect" on:click={connectDropbox} />
			{/if}
		</div>
	</div>

	<hr class="bg-surface-400/20 h-[1px] !border-0 rounded" />

	<div
		class="space-y-8 border-l-8 pl-4 {user.connections.minio
			? 'border-success-500/50'
			: 'border-error-500/50'}"
	>
		<h3 class="h3">Minio</h3>
		<div class="md:pl-8">
			{#if user.connections.minio}
				<p class="flex items-center gap-4">
					<Fa icon={faCheck} />
					<span>Read files and folders contained in the <strong>{access}</strong> folder</span>
				</p>
				<p class="flex items-center gap-4">
					<Fa icon={faCheck} />
					<span>Create files and folders in the <strong>{access}</strong> folder</span>
				</p>
				<form action="?/api/auth" method="GET" class="mt-4">
					<ActionButton icon={faTrash} text="Revoke access" />
				</form>
			{/if}
		</div>
	</div>
</section>
