<script lang="ts">
	import { goto } from '$app/navigation'
	import { Api, makeApiCall } from '$lib/duui/utils/api'
	import { info } from '$lib/duui/utils/ui.js'
	import { userSession } from '$lib/store.js'
	import ActionButton from '$lib/svelte/widgets/action/ActionButton.svelte'
	import Checkbox from '$lib/svelte/widgets/input/Checkbox.svelte'
	import Dropdown from '$lib/svelte/widgets/input/Dropdown.svelte'
	import Secret from '$lib/svelte/widgets/input/Secret.svelte'
	import Text from '$lib/svelte/widgets/input/TextInput.svelte'
	import {
		faAdd,
		faCheck,
		faFilePen,
		faFileText,
		faKey,
		faLink,
		faRefresh,
		faTrash,
		faXmarkCircle
	} from '@fortawesome/free-solid-svg-icons'
	import {
		clipboard,
		getModalStore,
		getToastStore,
		type ModalSettings
	} from '@skeletonlabs/skeleton'
	import Fa from 'svelte-fa'

	export let data
	const { user, dropbBoxURL } = data

	let connections = {
		dropbox: false,
		minio: false,
		mongodb: true
	}

	const toastStore = getToastStore()

	let minioEndpoint: string = $userSession?.minio?.endpoint || ''
	let minioAccessKey: string = $userSession?.minio?.access_key || ''
	let minioSecretKey: string = $userSession?.minio?.secret_key || ''

	let mongoDBConnectionURI: string = 'mongodb+srv://TestUser:1234@testcluster.727ylpr.mongodb.net/'

	const updateUser = async (data: object) => {
		const response = await fetch('/api/users', { method: 'PUT', body: JSON.stringify(data) })
		toastStore.trigger(info(response.statusText))
		return response
	}

	const generateApiKey = async () => {
		if (user.key) {
			const confirm = await showConfirmModal(
				'Regenarate API Key',
				'If you regenarate your API key, the old one will lose its access. Make sure to update your API key in all applications its used in.',
				'Regenerate'
			)

			if (!confirm) return
		}

		const response = await fetch('/api/users/auth/key', { method: 'PUT' })

		if (response.ok) {
			const userResponse = await response.json()
			console.log(userResponse)
			user.key = userResponse.user.key
		}
	}

	const showConfirmModal = async (
		title: string,
		message: string,
		confirmText: string = 'Confirm'
	) => {
		const response = new Promise<boolean>((resolve) => {
			const modal: ModalSettings = {
				type: 'component',
				component: 'confirmModal',
				meta: {
					title: title,
					body: message,
					confirmText: confirmText
				},
				response: (r: boolean) => {
					resolve(r)
				}
			}

			modalStore.trigger(modal)
		})

		return await response
	}

	const showDeleteModal = async (title: string, message: string) => {
		const response = new Promise<boolean>((resolve) => {
			const modal: ModalSettings = {
				type: 'component',
				component: 'deleteModal',
				meta: {
					title: title,
					body: message
				},
				response: (r: boolean) => {
					resolve(r)
				}
			}

			modalStore.trigger(modal)
		})

		return await response
	}

	const deleteApiKey = async () => {
		const confirm = await showDeleteModal(
			'Delete API Key',
			'Deleting your API Key remove the ability to make requests with it. You can always generate a new one here.'
		)

		if (!confirm) return

		const response = await fetch('/api/users/auth/key', { method: 'DELETE' })

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

	$: {
		if (!$userSession) {
			connections = { dropbox: false, minio: false, mongodb: false }
		} else {
			connections = {
				dropbox: !!$userSession?.dropbox?.access_token && !!$userSession?.dropbox?.refresh_token,
				minio: !$userSession?.minio
					? false
					: !!$userSession?.minio?.access_key &&
					  !!$userSession?.minio?.secret_key &&
					  !!$userSession?.minio?.endpoint,
				mongodb: true
			}
		}
	}

	let name: string = 'Temp'
</script>

<svelte:head>
	<title>Account</title>
</svelte:head>

<div class="grid md:grid-cols-2 gap-4 max-w-7xl md:py-16">
	<div class="section-wrapper p-8 space-y-4 scroll-mt-4" id="profile">
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
	</div>
	<div class="space-y-4">
		<div class="section-wrapper p-8 space-y-8 scroll-mt-4" id="authorization">
			<div class="flex items-center justify-between gap-4">
				<h2 class="h3 font-bold">API Key</h2>
				<Fa icon={faKey} size="lg" />
			</div>
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
					<button class="button-error" on:click={revokeDropboxAccess}>
						<Fa icon={faXmarkCircle} />
						<span>Revoke access</span>
					</button>
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
				{#if connections.minio}
					<p>Your account has been connected to Minio / AWS successfully.</p>
				{:else}
					<p>Enter your AWS credentials below to establish a connection.</p>
				{/if}
				<Text label="Endpoint" style="grow" name="endpoint" bind:value={minioEndpoint} />
				<Secret label="Access Key" name="accessKey" bind:value={minioAccessKey} />
				<Secret label="Secret Key" name="secretKey" bind:value={minioSecretKey} />
			</div>
			<div class="flex gap-4 justify-between">
				<button class="button-primary" on:click={connectMinio}>
					<Fa icon={connections.minio ? faRefresh : faLink} />
					<span>{connections.minio ? 'Update' : 'Connect'}</span>
				</button>
				{#if connections.minio}
					<button class="button-error" on:click={revokeMinioAccess}>
						<Fa icon={faXmarkCircle} />
						<span>Revoke access</span>
					</button>
				{/if}
			</div>
			<p class="text-surface-500 dark:text-surface-200">
				Visit
				<a href="https://min.io/" target="_blank" class="anchor">Minio</a>
				for further reading.
			</p>
		</div>
		<div class="section-wrapper p-8 grid grid-rows-[auto_1fr_auto] gap-8">
			<h2 class="h3 font-bold">MongoDB</h2>
			<div class="space-y-4">
				{#if connections.mongodb}
					<p>You are now ready to use Data stored in your MongoDB Cluster</p>
				{:else}
					<p>
						To allow DUUI to read and write specific Databases in your MongoDB Cluster follow these
						steps:
					</p>
					<ul class="list-decimal px-8 space-y-1">
						<li>
							Visit <a class="anchor" href="https://www.mongodb.com/cloud/atlas/register"
								>MongoDB Atlas</a
							> and sign in to your Account.
						</li>
						<li>
							Go to Organization {'>'} Project {'>'} Security {'>'} Database Access {'>'} Custom Roles.
						</li>
						<li>
							Add a new Custom Role that allows at least <span class="badge variant-soft-primary"
								>Find</span
							>
							and <span class="badge variant-soft-primary">Insert</span> access for the collections you
							need.
						</li>
						<li>
							Next go back to Organization {'>'} Project {'>'} Security {'>'} Database Access {'>'} Database
							Users.
						</li>
						<li>
							Add a new Database User, enter a username, password and select the Custom Role you
							just created.
						</li>
						<li>
							Optionally you can check the <span class="badge variant-soft-primary"
								>Temporary User</span
							>
							settings to allow only temporary access for DUUI.
						</li>
					</ul>
				{/if}
				<Secret label="Connection URI" name="connectionURI" bind:value={mongoDBConnectionURI} />
			</div>
			<div class="flex gap-4 justify-between">
				<ActionButton
					text={connections.mongodb ? 'Update' : 'Connect'}
					icon={connections.mongodb ? faRefresh : faLink}
					on:click={() => updateUser({ mongoDBConnectionURI: mongoDBConnectionURI })}
				/>
				<!-- {#if connections.mongodb}
					<ActionButton
						icon={faTrash}
						text="Revoke access"
						variant="variant-filled-error dark:variant-soft-error"
						on:click={revokeMinioAccess}
					/>
				{/if} -->
			</div>
			<p class="text-surface-500 dark:text-surface-200">
				Visit
				<a href="https://www.mongodb.com" target="_blank" class="anchor">MongoDB</a>
				for further reading.
			</p>
		</div>
	</div>
</div>
