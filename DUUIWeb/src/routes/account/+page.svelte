<script lang="ts">
	import { goto } from '$app/navigation'
	import { COLORS } from '$lib/config.js'
	import { successToast } from '$lib/duui/utils/ui.js'
	import { userSession } from '$lib/store.js'
	import Dropdown from '$lib/svelte/components/Input/Dropdown.svelte'
	import Password from '$lib/svelte/components/Input/Password.svelte'
	import Secret from '$lib/svelte/components/Input/Secret.svelte'
	import Text from '$lib/svelte/components/Input/TextInput.svelte'
	import { showConfirmationModal } from '$lib/svelte/utils/modal.js'
	import {
		faAdd,
		faCheck,
		faFilePen,
		faFileText,
		faLink,
		faRefresh,
		faTrash,
		faXmarkCircle
	} from '@fortawesome/free-solid-svg-icons'
	import {
		RadioGroup,
		RadioItem,
		clipboard,
		getModalStore,
		getToastStore
	} from '@skeletonlabs/skeleton'
	import { onMount } from 'svelte'
	import Fa from 'svelte-fa'

	export let data
	let { user, registered, dropbBoxURL, theme, users } = data
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

	const updateTheme = async (color: string) => {
		const response = await fetch(`/api/theme?color=${color}`, {
			method: 'PUT'
		})

		if (response.ok) {
			const result = await response.json()
			theme = +result.theme
		}
	}

	const themes = Object.keys(COLORS)
	$: {
		try {
			const body = document.body
			body.dataset.theme = 'theme-' + themes[theme]
		} catch (err) {}
	}

	const updateUser = async (data: object) => {
		const response = await fetch('/api/users', { method: 'PUT', body: JSON.stringify(data) })
		if (response.ok) {
			toastStore.trigger(successToast('Update successful'))
		}
		return response
	}

	const deleteAccount = async () => {
		const confirm = await showConfirmationModal(
			{
				title: 'Delete Account',
				message:
					'Deleting your Account also deletes all pipelines and processes ever created. Are you sure?',
				textYes: 'Delete'
			},
			modalStore
		)

		if (!confirm) return

		await fetch('/api/users', {
			method: 'DELETE'
		})
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

	onMount(() => {
		if (registered) {
			modalStore.trigger({
				type: 'component',
				component: 'welcomeModal'
			})
		}
	})

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
	let email: string = $userSession?.email || ''

	const deleteUser = async (user: { oid: string; email: string; role: string }) => {
		const confirm = await showConfirmationModal(
			{
				title: 'Delete Account',
				message:
					'Deleting the Account also deletes all pipelines and processes ever created. Are you sure?',
				textYes: 'Delete'
			},
			modalStore
		)

		if (!confirm) return

		const response = await fetch('/api/users/admin', {
			method: 'DELETE',
			body: JSON.stringify(user)
		})

		if (response.ok) {
			users = users.filter((item: { oid: string }) => item.oid !== user.oid)
		}
	}

	const updateRole = async (user: { oid: string; email: string; role: string }) => {
		const response = await fetch('/api/users/admin', { method: 'PUT', body: JSON.stringify(user) })
		if (response.ok) {
			toastStore.trigger(successToast('Update successful'))
		}
	}
</script>

<svelte:head>
	<title>Account</title>
</svelte:head>

<div class="gap-4 max-w-7xl md:py-16 grid md:grid-cols-2 items-start">
	<div class="space-y-4">
		<div class="section-wrapper p-8 space-y-4">
			<h2 class="h3">Profile</h2>
			<Text label="E-Mail" name="email" readonly={true} bind:value={email} />

			<div class="label">
				<p class="form-label">Theme</p>
				<RadioGroup
					class="grid grid-cols-2 gap-2 p-2 section-wrapper w-full"
					active="variant-filled-primary"
					padding="p-4"
				>
					<RadioItem bind:group={theme} name="blue" value={0} on:click={() => updateTheme('blue')}
						>Blue</RadioItem
					>
					<RadioItem bind:group={theme} name="red" value={1} on:click={() => updateTheme('red')}
						>Red</RadioItem
					>
					<RadioItem
						bind:group={theme}
						name="purple"
						value={2}
						on:click={() => updateTheme('purple')}>Purple</RadioItem
					>
					<RadioItem bind:group={theme} name="green" value={3} on:click={() => updateTheme('green')}
						>Green</RadioItem
					>
				</RadioGroup>
			</div>

			<!-- <button
			class="button-neutral"
			disabled={!name}
			on:click={() =>
				updateUser({
					name: name
				})}
		>
			<Fa icon={faCheck} />
			<span>Save</span>
		</button> -->
		</div>
		{#if users}
			<div class="section-wrapper p-8 space-y-4">
				<h3 class="h3">Users</h3>
				<div class="space-y-4">
					{#each users as user}
						<div class="grid grid-cols-[1fr_auto] items-end justify-between gap-4">
							<Dropdown
								on:change={() => updateRole(user)}
								label={user.email}
								bind:value={user.role}
								options={['User', 'Admin', 'Trial']}
							/>
							<button class="button-error button-modal" on:click={() => deleteUser(user)}>
								<Fa icon={faTrash} />
								<span>Delete</span>
							</button>
						</div>
					{/each}
				</div>
			</div>
		{/if}
	</div>

	<div class="space-y-4">
		<div class="section-wrapper p-8 space-y-8 scroll-mt-16" id="authorization">
			<h2 class="h3">API Key</h2>
			<div class="space-y-8">
				{#if connections.key}
					<div class="space-y-2">
						<Secret value={$userSession?.connections.key} style="pt-2" />
						<div
							class="grid grid-cols-3 items-center justify-center section-wrapper divide-x divider text-sm"
						>
							<button
								class="button-neutral !justify-center !border-none !rounded-none"
								use:clipboard={$userSession?.connections.key || ''}
								on:click={() => {
									toastStore.trigger(successToast('Copied!'))
								}}
							>
								Copy
							</button>
							<button
								class="button-neutral !justify-center !border-y-0 !rounded-none !border-x border-color"
								on:click={generateApiKey}
							>
								Regenerate
							</button>
							<button
								class="button-neutral !justify-center !border-none !rounded-none"
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
			<h2 class="h3 scroll-mt-16"" id="dropbox">Dropbox</h2>
			<div class="space-y-8">
				{#if connections.dropbox}
					<p>Your Dropbox account has been connected successfully.</p>
					<div>
						<p class="flex-center-4">
							<Fa icon={faCheck} size="lg" class="text-primary-500" />
							<span
								>Read files and folders contained in your <strong>Dropbox Account</strong>
							</span>
						</p>
						<p class="flex-center-4 mb-4">
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
						<p class="flex-center-4 mb-4">
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
			<h2 class="h3 scroll-mt-16" id="minio">Minio / AWS</h2>
			<div class="space-y-4">
				{#if connections.minio}
					<p>Your account has been connected to Minio / AWS successfully.</p>
				{:else}
					<p>Enter your AWS credentials below to establish a connection.</p>
				{/if}
				<Text
					help="The correct endpoint is the s3 API endpoint. Do not the Minio Console endpoint!"
					label="Endpoint"
					style="grow"
					name="endpoint"
					bind:value={minioEndpoint}
				/>
				<Password label="Username (Access Key)" name="accessKey" bind:value={minioAccessKey} />
				<Password label="Password (Secret Key)" name="secretKey" bind:value={minioSecretKey} />
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
	<div
		class="section-wrapper p-8 space-y-8 scroll-mt-4 flex justify-center items-center md:col-span-2"
	>
		<button class="button-error" on:click={deleteAccount}>
			<Fa icon={faTrash} />
			<span>Delete Account</span>
		</button>
	</div>
</div>
