<script lang="ts">
	import { successToast } from '$lib/duui/utils/ui.js'
	import { userSession } from '$lib/store.js'
	import { showConfirmationModal } from '$lib/svelte/utils/modal.js'
	import Secret from '$lib/svelte/components/Secret.svelte'
	import Text from '$lib/svelte/components/TextInput.svelte'
	import {
		faAdd,
		faCheck,
		faFilePen,
		faFileText,
		faLink,
		faRefresh,
		faSave,
		faXmarkCircle
	} from '@fortawesome/free-solid-svg-icons'
	import { clipboard, getModalStore, getToastStore } from '@skeletonlabs/skeleton'
	import Fa from 'svelte-fa'

	export let data
	const { user, dropbBoxURL } = data
	const toastStore = getToastStore()

	if (user && $userSession) {
		$userSession.preferences = user.preferences
		$userSession.connections = user.connections
	}

	let connections = {
		dropbox:
			$userSession?.connections.dropbox.access_token !== null &&
			$userSession?.connections.dropbox.refresh_token !== null,

		minio:
			$userSession?.connections.minio.access_key !== null &&
			$userSession?.connections.minio.endpoint !== null &&
			$userSession?.connections.minio.secret_key !== null,
		key: $userSession?.connections.key != null
	}

	let minioAccessKey: string = $userSession?.connections.minio.access_key || ''
	let minioEndpoint: string = $userSession?.connections.minio.endpoint || ''
	let minioSecretKey: string = $userSession?.connections.minio.secret_key || ''

	const updateUser = async (data: object) => {
		const response = await fetch('/api/users', { method: 'PUT', body: JSON.stringify(data) })
		if (response.ok) {
			toastStore.trigger(successToast('Update successful'))
		}
		return response
	}

	const generateApiKey = async () => {
		if (user.connections.key) {
			const confirm = await showConfirmationModal(
				{
					title: 'Regenerate API Key',
					message:
						'If you regenarate your API key, the current one will not work anymore. Make sure to update your API key in all applications its used in.',
					textYes: 'Regenerate'
				},
				modalStore
			)

			if (!confirm) return
		}

		const response = await fetch('/api/users/auth/key', { method: 'PUT' })

		if (response.ok && $userSession) {
			const item = await response.json()
			$userSession.connections.key = item.user.connections.key
			connections.key = true
		}
	}

	const deleteApiKey = async () => {
		const confirm = await showConfirmationModal(
			{
				title: 'Delete API Key',
				message:
					'Deleting your API Key remove the ability to make requests with it. You can always generate a new one here.',
				textYes: 'Delete'
			},
			modalStore
		)

		if (!confirm) return

		const response = await fetch('/api/users/auth/key', { method: 'DELETE' })

		if (response.ok && $userSession) {
			$userSession.connections.key = null
			connections.key = false
		}
	}

	const startDropboxOauth = async () => {
		window.location.href = (await dropbBoxURL).toString()
	}

	const modalStore = getModalStore()

	const deleteDropboxAccess = async () => {
		const confirm = await showConfirmationModal(
			{
				title: 'Delete Access for Dropbox',
				message: `Are you sure you want to revoke access?
					 You will have to go through the OAuth process again to reconnect.`,
				textYes: 'Delete'
			},
			modalStore
		)

		if (!confirm) return

		const response = await updateUser({
			'connections.dropbox.access_token': null,
			'connections.dropbox.refresh_token': null
		})

		if (response.ok) {
			connections.dropbox = false
		}
	}

	const revokeMinioAccess = async () => {
		const confirm = await showConfirmationModal(
			{
				title: 'Delete Access for Min.io',
				message: `Are you sure you want to delete access?`,
				textYes: 'Delete'
			},
			modalStore
		)

		if (!confirm) return

		const response = await updateUser({
			'connections.minio.endpoint': null,
			'connections.minio.access_key': null,
			'connections.minio.secret_key': null
		})

		if (response.ok) {
			connections.minio = false
			minioEndpoint = ''
			minioAccessKey = ''
			minioSecretKey = ''
		}
	}

	$: {
		if (!$userSession) {
			connections = { dropbox: false, minio: false, key: false }
		} else {
			connections = {
				dropbox:
					$userSession?.connections.dropbox.access_token !== null &&
					$userSession?.connections.dropbox.refresh_token !== null,

				minio:
					$userSession?.connections.minio.access_key !== null &&
					$userSession?.connections.minio.endpoint !== null &&
					$userSession?.connections.minio.secret_key !== null,
				key: $userSession?.connections.key != null
			}
		}
	}
	let name: string = $userSession?.name || ''
</script>

<svelte:head>
	<title>Account</title>
</svelte:head>

<div class="gap-4 max-w-7xl md:py-16 grid md:grid-cols-2 items-start">
	<div class="section-wrapper p-8 space-y-4">
		<h2 class="h3 font-bold">Profile</h2>
		<Text label="Name" name="name" bind:value={name} />
		<button
			class="button-neutral"
			disabled={!name}
			on:click={() =>
				updateUser({
					name: name
				})}
		>
			<Fa icon={faCheck} />
			<span>Save</span>
		</button>
	</div>

	<div class="space-y-4">
		<div class="section-wrapper p-8 space-y-8 scroll-mt-4" id="authorization">
			<h2 class="h3 font-bold">API Key</h2>
			<div class="space-y-8">
				{#if connections.key}
					<div class="space-y-2">
						<Secret value={$userSession?.connections.key} style="pt-2" />
						<div class="flex items-center gap-2 text-sm">
							<p
								class="text-primary-500 hover:text-primary-400 cursor-pointer transition-colors px-2 border-r"
								use:clipboard={$userSession?.connections.key || ''}
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
					<button class="button-primary" on:click={generateApiKey}>
						<Fa icon={faAdd} />
						<span>Generate</span>
					</button>
				{/if}
			</div>
			<p class="text-surface-500 dark:text-surface-200">
				Check the
				<a href="/documentation/api" target="_blank" class="anchor">API Reference</a>
				for examples and use cases.
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
								>Read files and folders contained in your <strong>Dropbox Account</strong>
							</span>
						</p>
						<p class="flex items-center gap-4 mb-4">
							<Fa icon={faCheck} size="lg" class="text-primary-500" />
							<span>Create files and folders in your <strong>Dropbox Account</strong> </span>
						</p>
					</div>
					<div class="grid md:flex justify-between gap-4">
						<button class="button-neutral" on:click={startDropboxOauth}>
							<Fa icon={faLink} />
							<span>Reconnect</span>
						</button>
						<button class="button-error" on:click={deleteDropboxAccess}>
							<Fa icon={faXmarkCircle} />
							<span>Delete</span>
						</button>
					</div>
				{:else}
					<p class="mb-8">
						By connecting Dropbox and DUUI you can directly read and write data from and to your
						Dropbox storage. After a succesfull OAuth2 authorization at <span class="font-bold"
							>Dropbox</span
						> an app folder called DUUI is created in your storage that is used as the root folder for
						read and write operations.
					</p>
					<div class="space-y-2">
						<p class="flex items-center gap-[22px]">
							<Fa icon={faFileText} size="lg" class="text-primary-500" />
							<span
								>Read files and folders contained in your <strong>Dropbox Storage</strong>
							</span>
						</p>
						<p class="flex items-center gap-4 mb-4">
							<Fa icon={faFilePen} size="lg" class="text-primary-500" />
							<span>Create files and folders in your <strong>Dropbox Storage</strong> </span>
						</p>
					</div>
					<button class="button-neutral" on:click={startDropboxOauth}>
						<Fa icon={faLink} />
						<span>Connect</span>
					</button>
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
				<Text label="Username (Access Key)" name="accessKey" bind:value={minioAccessKey} />
				<Text label="Password (Secret Key)" name="secretKey" bind:value={minioSecretKey} />
			</div>
			<div class="grid md:flex justify-between gap-4">
				<button
					class="button-neutral"
					disabled={!minioEndpoint || !minioAccessKey || !minioSecretKey}
					on:click={() =>
						updateUser({
							'connections.minio.endpoint': minioEndpoint,
							'connections.minio.access_key': minioAccessKey,
							'connections.minio.secret_key': minioSecretKey
						})}
				>
					<Fa icon={connections.minio ? faRefresh : faLink} />
					<span>{connections.minio ? 'Update' : 'Connect'}</span>
				</button>
				{#if connections.minio}
					<button class="button-error" on:click={revokeMinioAccess}>
						<Fa icon={faXmarkCircle} />
						<span>Delete</span>
					</button>
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
