<script lang="ts">
	import ActionButton from '$lib/svelte/widgets/action/ActionButton.svelte'
	import { makeApiCall, Api } from '$lib/duui/utils/api'
	import {
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
	import Text from '$lib/svelte/widgets/input/TextInput.svelte'
	import { userSession } from '$lib/store.js'
	import Secret from '$lib/svelte/widgets/input/Secret.svelte'
	import Checkbox from '$lib/svelte/widgets/input/Checkbox.svelte'
	import Dropdown from '$lib/svelte/widgets/input/Dropdown.svelte'

	export let data
	const { user, dropbBoxURL } = data

	let connections = {
		dropbox: false,
		minio: false
	}

	let minioEndpoint: string = user.minio.endpoint || ''
	let minioAccessKey: string = user.minio.access_key || ''
	let minioSecretKey: string = user.minio.secret_key || ''

	const generateApiKey = async () => {
		const response = await fetch('/auth', {
			method: 'PUT'
		})

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

	const startDropboxOauth = async () => {
		goto(dropbBoxURL.toString())
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

	$: connections = {
		dropbox: !!user.dropbox?.access_token && !!user.dropbox?.refresh_token,
		minio: !!user.minio?.access_key && !!user.minio?.secret_key && !!user.minio?.endpoint
	}

	let name: string = 'Temp'
</script>

<svelte:head>
	<title>Account</title>
</svelte:head>

<div class="grid md:grid-cols-2 gap-4 max-w-7xl md:py-16">
	<section class="section-wrapper p-8 space-y-4 scroll-mt-4" id="profile">
		<h2 class="h3 font-bold">Profile</h2>
		<Text disabled label="Name" name="name" bind:value={name} />
		<Text disabled label="E-Mail" name="email" bind:value={user.email} />
		<Dropdown
			label="Language"
			name="language"
			bind:value={user.preferences.language}
			options={['English', 'German']}
		/>
		<Checkbox
			label="Show hints when using the editor."
			name="hints"
			bind:checked={user.preferences.tutorial}
		/>
		<Checkbox
			label="Enable notifications to get informed when a pipeline is finished."
			name="notifications"
			bind:checked={user.preferences.notifications}
		/>
	</section>
	<div class="grid gap-4">
		<div
			class="section-wrapper p-8 grid grid-rows-[auto_1fr_auto] gap-8 scroll-mt-4"
			id="authorization"
		>
			<h2 class="h3 font-bold">API Key</h2>
			<div class="space-y-8">
				{#if user.key}
					<div class="space-y-2">
						<Secret value={user.key} />
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
			</div>
			<p class="text-surface-500 dark:text-surface-200">
				Visit the
				<a href="/documentation/api" target="_blank" class="anchor">API Reference</a>
				for further reading.
			</p>
		</div>
		<div class="section-wrapper p-8 grid grid-rows-[auto_1fr_auto] gap-8">
			<h2 class="h3 font-bold">Dropbox</h2>
			<div class="space-y-8">
				{#if connections.dropbox}
					<p>Your Dropbox account has been connected successfully.</p>
					<div>
						<p class="flex items-center gap-4">
							<Fa icon={faCheck} size="lg" class="text-primary-500" />
							<span
								>Read files and folders contained in your <strong>Dropbox Storage</strong>
							</span>
						</p>
						<p class="flex items-center gap-4 mb-4">
							<Fa icon={faCheck} size="lg" class="text-primary-500" />
							<span>Create files and folders in your <strong>Dropbox Storage</strong> </span>
						</p>
					</div>

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
					<ActionButton icon={faLink} text="Connect" on:click={startDropboxOauth} />
				{/if}
			</div>
			<p class="text-surface-500 dark:text-surface-200">
				Visit
				<a
					href="https://help.dropbox.com/de-de/integrations/third-party-apps"
					target="_blank"
					class="anchor">Dropbox Apps</a
				>
				for further reading.
			</p>
		</div>
		<div class="section-wrapper p-8 grid grid-rows-[auto_1fr_auto] gap-8">
			<h2 class="h3 font-bold">Minio / AWS</h2>
			<div class="space-y-4">
				<p>Your account has been connected to Minio / AWS successfully.</p>
				<Text label="Endpoint" style="grow" name="endpoint" bind:value={minioEndpoint} />
				<Secret label="Access Key" name="accessKey" bind:value={minioAccessKey} />
				<Secret label="Secret Key" name="secretKey" bind:value={minioSecretKey} />
			</div>
			<div class="flex gap-4 justify-between">
				<ActionButton
					text={connections.minio ? 'Update' : 'Connect'}
					icon={connections.minio ? faRefresh : faLink}
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
			<p class="text-surface-500 dark:text-surface-200">
				Visit
				<a href="https://min.io/" target="_blank" class="anchor">Minio</a>
				for further reading.
			</p>
		</div>
	</div>
</div>
