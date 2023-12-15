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
	import { clipboard, getModalStore, type ModalSettings } from '@skeletonlabs/skeleton'
	import Fa from 'svelte-fa'
	import Password from '$lib/svelte/widgets/input/Password.svelte'
	import { goto } from '$app/navigation'
	import { storage } from '$lib/store.js'
	import Text from '$lib/svelte/widgets/input/Text.svelte'

	export let data
	const { user, session, dropbBoxURL } = data


	let access: string = 'Cedric-Test-App'
	let minioEndpoint: string = user.minio.endpoint || ''
	let minioAccessKey: string = user.minio.access_key || ''
	let minioSecretKey: string = user.minio.secret_key || ''

	const generateApiKey = async () => {
		const response = await makeApiCall(Api.Authentication, 'GET')
		if (response.ok) {
			const data = await response.json()
			user.key = data.key
		}
	}

	const deleteApiKey = async () => {
		const response = await makeApiCall(Api.Authentication, 'DELETE')
		if (response.ok) {
			user.key = ''
		}
	}

	const connectDropbox = async () => {
		$storage = session
		goto(dropbBoxURL.toString())
		const response = await makeApiCall(Api.Dropbox, 'POST', {})
		if (response.ok) {
			user.connections.dropbox = true
		}
	}

	const modalStore = getModalStore()

	const revokeDropboxAccess = async () => {
		new Promise<boolean>((resolve) => {
			const modal: ModalSettings = {
				type: 'component',
				component: 'deleteModal',
				meta: {
					title: 'Revoke Access for Dropbox',
					body: `Are you sure you want to revoke access?`
				},
				response: (r: boolean) => {
					resolve(r)
				}
			}

			modalStore.trigger(modal)
		}).then(async (accepted: boolean) => {
			if (!accepted) return

			const response = await makeApiCall(Api.Dropbox, 'DELETE', {})

			if (response.ok) {
				user.connections.dropbox = false
			}
		})
	}

	const connectMinio = async () => {
		const response = await makeApiCall(Api.Minio, 'POST', {
			endpoint: minioEndpoint,
			accessKey: minioAccessKey,
			secretKey: minioSecretKey
		})

		if (response.ok) {
			const data = await response.json()
			minioEndpoint = data.endpoint
			minioAccessKey = data.access_key
			minioSecretKey = data.secret_key
		}
	}

	const revokeMinioAccess = async () => {
		new Promise<boolean>((resolve) => {
			const modal: ModalSettings = {
				type: 'component',
				component: 'deleteModal',
				meta: {
					title: 'Revoke Access for Min.io',
					body: `Are you sure you want to revoke access?`
				},
				response: (r: boolean) => {
					resolve(r)
				}
			}

			modalStore.trigger(modal)
		}).then(async (accepted: boolean) => {
			if (!accepted) return

			const response = await makeApiCall(Api.Minio, 'DELETE', {})

			if (response.ok) {
				user.connections.minio = false
				minioEndpoint = ''
				minioAccessKey = ''
				minioSecretKey = ''
			}
		})
	}

</script>

<section class="space-y-8">
	<div
		class="space-y-8 border-l-8 pl-4 {user.key ? 'border-success-500/50' : 'border-error-500/50'}"
	>
		<h3 class="h3">Api key</h3>
		<div class="md:pl-8">
			{#if user.key}
				<p>Don't share this key, anyone with this key can make api calls in your name.</p>
				<div class="flex flex-col-reverse md:flex-row md:items-center gap-4 mt-4">
					<button class="btn rounded-none variant-soft-primary mt-1" use:clipboard={user.key}>
						<Fa icon={faCopy} />
						<span>Copy</span>
					</button>
					<Password style="grow" readonly={true} bind:value={user.key} />
				</div>
			{/if}
			<div class="mt-4 flex gap-4 justify-between">
				<ActionButton
					text={user.key ? 'Regenerate' : 'Generate API key'}
					icon={user.key ? faRefresh : faAdd}
					on:click={generateApiKey}
				/>
				{#if user.key}
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
		<div class="md:pl-8 grid">
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
					_class="ml-auto"
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
				<ActionButton _class="mr-auto" icon={faLink} text="Connect" on:click={connectDropbox} />
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
		<div class="md:pl-8 grid">
			{#if user.connections.minio}
				<p class="flex items-center gap-4">
					<Fa icon={faCheck} />
					<span>Read files and folders contained in the <strong>{access}</strong> folder</span>
				</p>
				<p class="flex items-center gap-4">
					<Fa icon={faCheck} />
					<span>Create files and folders in the <strong>{access}</strong> folder</span>
				</p>
			{/if}
			<div class="space-y-4 mt-4">
				<div class="flex flex-col-reverse md:flex-row md:items-end gap-2">
					{#if minioEndpoint}
						<button class="btn rounded-none variant-soft-primary mt-1" use:clipboard={user.key}>
							<Fa icon={faCopy} />
							<span>Copy</span>
						</button>
					{/if}
					<Text label="Endpoint" style="grow" name="endpoint" bind:value={minioEndpoint} />
				</div>
				<div class="flex flex-col-reverse md:flex-row md:items-end gap-2">
					{#if user.minio.access_key}
						<button class="btn rounded-none variant-soft-primary mt-1" use:clipboard={user.key}>
							<Fa icon={faCopy} />
							<span>Copy</span>
						</button>
					{/if}
					<Password label="Access Key" name="accessKey" style="grow" bind:value={minioAccessKey} />
				</div>
				<div class="flex flex-col-reverse md:flex-row md:items-end gap-2">
					{#if user.minio.secret_key}
						<button class="btn rounded-none variant-soft-primary mt-1" use:clipboard={user.key}>
							<Fa icon={faCopy} />
							<span>Copy</span>
						</button>
					{/if}
					<Password label="Secret Key" name="secretKey" style="grow" bind:value={minioSecretKey} />
				</div>
				<div class="flex gap-4 justify-between">
					<ActionButton
						text={user.connections.minio ? 'Update' : 'Connect'}
						icon={user.connections.minio ? faRefresh : faAdd}
						on:click={connectMinio}
					/>
					{#if user.connections.minio}
						<ActionButton
							icon={faTrash}
							text="Revoke access"
							variant="variant-filled-error dark:variant-soft-error"
							on:click={revokeMinioAccess}
						/>
					{/if}
				</div>
			</div>
		</div>
	</div>
</section>
