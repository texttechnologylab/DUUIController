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
		faLink,
		faWarning
	} from '@fortawesome/free-solid-svg-icons'
	import { clipboard, getModalStore, type ModalSettings } from '@skeletonlabs/skeleton'
	import Fa from 'svelte-fa'
	import Password from '$lib/svelte/widgets/input/Password.svelte'
	import { goto } from '$app/navigation'
	import Text from '$lib/svelte/widgets/input/TextInput.svelte'

	export let data
	const { user, dropbBoxURL } = data

	let minioEndpoint: string = user.minio.endpoint || ''
	let minioAccessKey: string = user.minio.access_key || ''
	let minioSecretKey: string = user.minio.secret_key || ''

	const connections = {
		dropbox: !!user.dropbox.access_token && !!user.dropbox.refresh_token,
		minio: !!minioEndpoint && !!minioAccessKey && !!minioSecretKey
	}

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
			user.key = ''
		}
	}

	const connectDropbox = async () => {
		goto(dropbBoxURL.toString())
		const response = await makeApiCall(Api.Dropbox, 'POST', {})
		if (response.ok) {
			connections.dropbox = true
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
					body: `Are you sure you want to revoke access?
					 You will have to go through the OAuth process again to reconnect.`
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
				connections.dropbox = false
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
				connections.minio = false
				minioEndpoint = ''
				minioAccessKey = ''
				minioSecretKey = ''
			}
		})
	}
</script>

<div class="grid lg:grid-cols-3 gap-4">
	<div class="section-wrapper">
		<h2 class="h3 px-4 p-3 font-bold {!!user.key ? 'variant-soft-success' : ''}">API Key</h2>
		<div class="p-4 space-y-8">
			{#if user.key}
				<div class="space-y-2">
					<Password readonly={true} bind:value={user.key} />
					<div class="flex items-center gap-2 text-sm">
						<p
							class="text-primary-500 hover:text-primary-400 cursor-pointer transition-colors px-2 border-r"
							use:clipboard={user.key}
						>
							Copy
						</p>
						<button
							class="text-primary-500 hover:text-primary-400 transition-colors pr-2 border-r"
							on:click={generateApiKey}
						>
							Regenerate
						</button>
						<button
							class="text-error-500 hover:text-error-400 transition-colors"
							on:click={deleteApiKey}
						>
							Delete
						</button>
					</div>
				</div>

				<p class="text-surface-500 dark:text-surface-200">
					Don't share this key! Anyone with this key can make api calls in your name.
				</p>
			{:else}
				<p class="text-surface-500 dark:text-surface-200">Generate a key to use the Api.</p>
				<ActionButton text="Generate" icon={faAdd} on:click={generateApiKey} />
			{/if}

			<p class="text-surface-500 dark:text-surface-200">
				Visit the
				<a href="/documentation/api" target="_blank" class="anchor">API Reference</a>
				for further reading.
			</p>
		</div>
	</div>
	<div class="section-wrapper">
		<h2 class="h3 px-4 p-3 font-bold {!!connections.dropbox ? 'variant-soft-success' : ''}">
			Dropbox
		</h2>
		<div class="p-4 space-y-8">
			{#if connections.dropbox}
				<p>Your Dropbox account has been connected successfully.</p>
				<p class="flex items-center gap-4">
					<Fa icon={faCheck} size="lg" class="text-primary-500" />
					<span>Read files and folders contained in your <strong>Dropbox Storage</strong> </span>
				</p>
				<p class="flex items-center gap-4 mb-4">
					<Fa icon={faCheck} size="lg" class="text-primary-500" />
					<span>Create files and folders in your <strong>Dropbox Storage</strong> </span>
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
					<Fa icon={faFileText} size="lg" class="text-primary-500" />
					<span>Read files and folders contained in your <strong>Dropbox Storage</strong> </span>
				</p>
				<p class="flex items-center gap-4 mb-4">
					<Fa icon={faFilePen} size="lg" class="text-primary-500" />
					<span>Create files and folders in your <strong>Dropbox Storage</strong> </span>
				</p>
				<ActionButton icon={faLink} text="Connect" on:click={connectDropbox} />
			{/if}
		</div>
	</div>
	<div class="section-wrapper">
		<h2 class="h3 px-4 p-3 font-bold {!!connections.minio ? 'variant-soft-success' : ''}">
			Minio / AWS
		</h2>
		<div class="p-4 space-y-4">
			<p>Your account has been connected to Minio / AWS successfully.</p>
			<Text label="Endpoint" style="grow" name="endpoint" bind:value={minioEndpoint} />
			<Password label="Access Key" name="accessKey" style="grow" bind:value={minioAccessKey} />
			<Password label="Secret Key" name="secretKey" style="grow" bind:value={minioSecretKey} />
		</div>
		<div class="flex gap-4 justify-between p-4">
			<ActionButton
				text={connections.minio ? 'Update' : 'Connect'}
				icon={connections.minio ? faRefresh : faAdd}
				on:click={connectMinio}
			/>
			{#if connections.minio}
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
