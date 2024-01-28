<script lang="ts">
	import { successToast } from '$lib/duui/utils/ui.js'
	import { showModal } from '$lib/utils/modal.js'
	import { userSession } from '$lib/store.js'
	import ActionButton from '$lib/svelte/widgets/action/ActionButton.svelte'
	import Secret from '$lib/svelte/widgets/input/Secret.svelte'
	import Text from '$lib/svelte/widgets/input/TextInput.svelte'
	import {
		faAdd,
		faArrowRight,
		faCheck,
		faFilePen,
		faFileText,
		faKey,
		faLink,
		faRefresh,
		faXmarkCircle
	} from '@fortawesome/free-solid-svg-icons'
	import {
		clipboard,
		getModalStore,
		getToastStore,
		popup,
		type PopupSettings
	} from '@skeletonlabs/skeleton'
	import Fa from 'svelte-fa'
	import { fly } from 'svelte/transition'

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
			const confirm = await showModal(
				{
					title: 'Regenerate API Key',
					message:
						'If you regenarate your API key, the old one will lose its access. Make sure to update your API key in all applications its used in.',
					confirmText: 'Regenerate'
				},
				'confirmModal',
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
		const confirm = await showModal(
			{
				title: 'Delete API Key',
				message:
					'Deleting your API Key remove the ability to make requests with it. You can always generate a new one here.'
			},
			'deleteModal',
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
		const confirm = await showModal(
			{
				title: 'Delete Access for Dropbox',
				message: `Are you sure you want to revoke access?
					 You will have to go through the OAuth process again to reconnect.`
			},
			'deleteModal',
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
		const confirm = await showModal(
			{
				title: 'Delete Access for Min.io',
				message: `Are you sure you want to delete access?`
			},
			'deleteModal',
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
	let name: string = 'Name'

	const popupUIMA: PopupSettings = {
		event: 'hover',
		target: 'popupUIMA',
		placement: 'top'
	}
</script>

<svelte:head>
	<title>Account</title>
</svelte:head>
<div
	data-popup="popupUIMA"
	in:fly={{ y: 50 }}
	class="variant-filled-primary max-w-[40ch] p-2 rounded-md text-sm"
>
	<p>
		UIMA is a framework by itself that defines a common structure for otherwise unstructured data,
		mainly in the form of natural written or spoken language.
	</p>
	<div class="arrow variant-filled-primary" />
</div>

<div class="gap-4 max-w-7xl md:py-16 grid md:grid-cols-2 items-start">
	<div class="section-wrapper p-8 space-y-4">
		<h2 class="h3 font-bold">Profile</h2>
		<Text label="Name" name="name" bind:value={name} />
		{#if $userSession && $userSession.preferences.step === 0}
			<p>Welcome to DUUI-Web!</p>

			<p>
				Since this is the first time you are here, are you interested in a short tour that explains
				what you can do?
			</p>
			<button
				class="button-primary"
				on:click={() => {
					updateUser({ 'preferences.tutorial': true, 'preferences.step': 0 })

					$userSession.preferences.tutorial = true
					$userSession.preferences.step = 0
				}}
			>
				Let's go
			</button>
		{:else if $userSession && $userSession.preferences.tutorial}
			<div>
				<h4 class="h4 mb-4">Alright! Let's get started.</h4>
				<p>
					<a class="anchor" href="https://github.com/texttechnologylab/DockerUnifiedUIMAInterface"
						>DUUI
					</a>
					is a Framework for big data analysis of natural language. Analysis is done through so-called
					<a class="anchor" href="/documentation#pipeline">Pipelines</a> that manage the flow of
					data through Analysis Engines according to
					<span class="cursor-pointer underline font-bold" use:popup={popupUIMA}>UIMA</span> standards.
				</p>
				<div class="flex gap-4 items-start justify-between mt-8">
					<p>
						New things are best learned when done - <br /> so let's focus on creating your first pipeline.
					</p>
					<a
						href="/pipelines"
						class="button-primary"
						on:click={() => {
							updateUser({ 'preferences.step': 1 })
							$userSession.preferences.step = 1
						}}
					>
						Continue <Fa icon={faArrowRight} /></a
					>
				</div>
			</div>
		{/if}
		<!-- <Text disabled label="E-Mail" name="email" bind:value={user.email} /> -->
		<!-- <Dropdown
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
		/> -->
	</div>

	<div class="space-y-4">
		<div class="section-wrapper p-8 space-y-8 scroll-mt-4" id="authorization">
			<div class="flex items-center justify-between gap-4">
				<h2 class="h3 font-bold">API Key</h2>
				<Fa icon={faKey} size="lg" />
			</div>
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
					<ActionButton text="Generate" icon={faAdd} on:click={generateApiKey} />
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
					<div class="flex justify-between">
						<button class="button-primary" on:click={startDropboxOauth}>
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
				<Secret label="Username (Access Key)" name="accessKey" bind:value={minioAccessKey} />
				<Secret label="Password (Secret Key)" name="secretKey" bind:value={minioSecretKey} />
			</div>
			<div class="flex gap-4 justify-between">
				<button
					class="button-primary"
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
