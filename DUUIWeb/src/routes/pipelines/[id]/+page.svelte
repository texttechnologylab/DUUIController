<script lang="ts">
	import { v4 as uuidv4 } from 'uuid'
	import { beforeNavigate, goto, onNavigate } from '$app/navigation'
	import PipelineComponent from '$lib/components/PipelineComponent.svelte'

	import {
		faArrowLeft,
		faCopy,
		faFileCircleCheck,
		faFileCircleXmark,
		faFileExport,
		faFilePen,
		faGears,
		faHourglassEnd,
		faHourglassStart,
		faLayerGroup,
		faPlus,
		faSave,
		faTrash,
		faUpRightAndDownLeftFromCenter,
		faWifi
	} from '@fortawesome/free-solid-svg-icons'
	import {
		type ModalSettings,
		getModalStore,
		tableMapperValues,
		Table
	} from '@skeletonlabs/skeleton'
	import { dndzone, type DndEvent } from 'svelte-dnd-action'
	import Fa from 'svelte-fa'
	import { flip } from 'svelte/animate'

	import { getToastStore } from '@skeletonlabs/skeleton'
	import type { TableSource } from '@skeletonlabs/skeleton'
	import type { PageServerData } from './$types'
	import { datetimeToString, progresAsPercent } from '$lib/utils/text'
	import { getDuration } from '$lib/utils/time'
	import { progressMaximum } from '$lib/duui/process'
	import type { DUUIComponent } from '$lib/duui/component'
	import { TabGroup, Tab } from '@skeletonlabs/skeleton'
	import { page } from '$app/stores'
	import { Api, makeApiCall } from '$lib/utils/api'
	import { info, success } from '$lib/utils/ui'

	export let data: PageServerData
	let { pipeline, processes } = data

	let hasChanges: boolean = false
	let name: string = pipeline.name
	let description: string = pipeline.description
	let components: DUUIComponent[] = pipeline.components

	let tabSet: number = +($page.url.searchParams.get('tab') || 0)

	let tableSource = processes.map((process, index, _) => {
		return {
			positon: index,
			startedAt: process.startTime ? datetimeToString(new Date(process.startTime)) : '',
			duration: getDuration(process.startTime, process.endTime),
			io: process.input.source + ' / ' + process.output.target,
			progress: progresAsPercent(process.progress, progressMaximum(process, pipeline)) + ' %',
			status: process.status,
			process: process
		}
	})

	let tableData: TableSource = {
		head: ['Start Time', 'Duration', 'Input / Output', 'Progress', 'Status'],
		body: tableMapperValues(tableSource, ['startedAt', 'duration', 'io', 'progress', 'status']),
		meta: tableMapperValues(tableSource, ['process'])
	}

	let flipDurationMs = 300

	function handleDndConsider(event: CustomEvent<DndEvent<DUUIComponent>>) {
		pipeline.components = event.detail.items
		pipeline.components = [...pipeline.components]
	}

	function handleDndFinalize(event: CustomEvent<DndEvent<DUUIComponent>>) {
		pipeline.components = event.detail.items
		pipeline.components = [...pipeline.components]
	}

	const modalStore = getModalStore()
	const toastStore = getToastStore()

	const updatePipeline = async () => {
		let response = await makeApiCall(Api.Pipelines, 'PUT', pipeline)
		if (response.ok) toastStore.trigger(success('Changes saved successfully'))
	}

	const deletePipeline = async () => {
		new Promise<boolean>((resolve) => {
			const modal: ModalSettings = {
				type: 'confirm',
				title: 'Delete Pipeline',
				buttonTextConfirm: 'Delete',
				body: `Are you sure you want to delete ${pipeline.name}?`,
				response: (r: boolean) => {
					resolve(r)
				}
			}
			modalStore.trigger(modal)
		}).then(async (accepted: boolean) => {
			if (!accepted) return

			let response = await makeApiCall(Api.Pipelines, 'DELETE', { id: pipeline.id })
			if (response.ok) {
				goto('/pipelines')
				toastStore.trigger(success('Pipeline deleted successfully'))
			}
		})
	}

	const copyPipeline = async () => {
		let response = await makeApiCall(Api.Pipelines, 'POST', pipeline)
		if (response.ok) {
			const data = await response.json()
			toastStore.trigger(info('Pipeline copied successfully'))
			goto(`/pipelines/${data.id}`)
		}
	}

	let startingService: boolean = false
	let stoppingService: boolean = false

	const manageService = async () => {
		if (startingService || stoppingService) return
		pipeline.serviceStartTime === 0 ? startService() : stopService()
	}

	const startService = async () => {
		startingService = true
		toastStore.trigger(info(`Starting Service ${pipeline.name}`))

		let response = await makeApiCall(Api.Services, 'POST', pipeline)

		if (response.ok) {
			const json = await response.json()
			pipeline.serviceStartTime = json.serviceStartTime
			toastStore.trigger(success(`Service ${pipeline.name} has been started`))
		}

		startingService = false
	}

	const stopService = async () => {
		stoppingService = true
		toastStore.trigger(info(`Stopping Service ${pipeline.name}`))

		let response = await makeApiCall(Api.Services, 'PUT', pipeline)

		if (response.ok) {
			pipeline.serviceStartTime = 0
			toastStore.trigger(success(`Service ${pipeline.name} has been stopped`))
		}

		stoppingService = false
	}

	const discardChanges = () => {
		pipeline.name = name
		pipeline.description = description
		pipeline.components = components
	}

	$: {
		hasChanges =
			name !== pipeline.name ||
			description !== pipeline.description ||
			components !== pipeline.components
	}
</script>

<div class="container h-full flex-col mx-auto flex gap-4 md:my-8">
	<!-- HEADER -->
	<h1 class="h2">{pipeline.name}</h1>
	<hr />
	<div class="flex items-center justify-start gap-4">
		<button on:click={() => goto('/pipelines')} class="btn rounded-md variant-soft-surface"
			><Fa icon={faArrowLeft} />
			<span class="hidden md:block">Back</span>
		</button>

		<button
			class="btn rounded-md variant-soft-surface"
			on:click={() => {
				return
				const jsonString = JSON.stringify(pipeline)
				const blob = new Blob([jsonString], { type: 'application/json' })
				const url = URL.createObjectURL(blob)
				const anchor = document.createElement('a')
				anchor.href = url
				anchor.download = `${pipeline.name}.json`
				document.body.appendChild(anchor)
				anchor.click()
				document.body.removeChild(anchor)
				URL.revokeObjectURL(url)
			}}
		>
			<Fa icon={faFileExport} />
			<span>Export</span>
			<!-- TODO: Dialog / Modal -->
		</button>

		<button class="btn rounded-md variant-soft-surface" on:click={copyPipeline}>
			<Fa icon={faCopy} />
			<span>Copy</span>
		</button>

		<button class="btn rounded-md variant-soft-surface" on:click={deletePipeline}>
			<Fa icon={faTrash} />
			<span>Delete</span>
		</button>

		<button
			class="btn rounded-md variant-soft-surface"
			on:click={() => goto('/process?pipeline=' + pipeline.id)}
		>
			<Fa icon={faPlus} />
			<span>New Run</span>
		</button>

		<button class="btn rounded-md variant-soft-surface ml-auto" on:click={manageService}>
			{#if startingService}
				<p>Instantiating</p>
				<Fa icon={faHourglassStart} class="animate-pulse" />
			{:else if stoppingService}
				<p>Shutting Down</p>
				<Fa icon={faHourglassEnd} class="animate-pulse" />
			{:else}
				<p>{pipeline.serviceStartTime !== 0 ? 'Online' : 'Offline'}</p>
				<Fa
					icon={faWifi}
					class={pipeline.serviceStartTime === 0 ? 'text-error-400' : 'text-success-400'}
				/>
			{/if}
		</button>
	</div>

	<div class="variant-soft-surface rounded-md shadow-lg">
		<TabGroup active="variant-filled-surface " border="none" rounded="rounded-md">
			<Tab bind:group={tabSet} name="settings" value={0}>
				<div class="flex items-center gap-4">
					<Fa icon={faGears} />
					<span>Settings</span>
				</div>
			</Tab>
			<Tab bind:group={tabSet} name="processes" value={1}
				><div class="flex items-center gap-4">
					<Fa icon={faLayerGroup} />
					<span>Runs</span>
				</div></Tab
			>
		</TabGroup>
	</div>

	{#if tabSet === 0}
		<div class="grid md:grid-cols-2 gap-4">
			<div class="flex flex-col gap-4 variant-soft-surface rounded-md shadow-lg p-4">
				<label class="label">
					<span>Name</span>
					<input
						bind:value={pipeline.name}
						class="border-2 input focus-within:outline-primary-400"
						type="text"
					/>
				</label>

				<label class="label">
					<span>Description</span>

					<textarea
						class="textarea border-2 input"
						rows="4"
						placeholder="Enter a description..."
						bind:value={pipeline.description}
					/>
				</label>

				<div class="flex justify-between">
					<button
						class="btn variant-soft-surface shadow-lg {hasChanges ? 'inline-flex' : 'hidden'}"
						on:click={updatePipeline}
					>
						<Fa icon={faFilePen} />
						<span>Save Changes</span>
					</button>

					<button
						class="btn variant-soft-surface shadow-lg {hasChanges ? 'inline-flex' : 'hidden'}"
						on:click={discardChanges}
					>
						<Fa icon={faFileCircleXmark} />
						<span>Discard Changes</span>
					</button>
				</div>
			</div>
			<div class="space-y-4 variant-soft-surface rounded-md shadow-lg p-4">
				<ul
					use:dndzone={{ items: pipeline.components, dropTargetStyle: {} }}
					on:consider={(event) => handleDndConsider(event)}
					on:finalize={(event) => handleDndFinalize(event)}
					class="grid gap-4"
				>
					{#each pipeline.components as component (component.id)}
						<div animate:flip={{ duration: flipDurationMs }}>
							<PipelineComponent
								{component}
								on:update={updatePipeline}
								on:delete={(event) => {
									pipeline.components = pipeline.components.filter(
										(c) => c !== event.detail.component
									)
									components = pipeline.components
								}}
							/>
						</div>
					{/each}
				</ul>
			</div>
		</div>
	{:else if tabSet === 1}
		<Table
			class="md:block rounded-md shadow-lg variant-soft-surface"
			source={tableData}
			interactive
			on:selected={(e) => goto('/process/' + e.detail[0].id)}
		/>

		<div class="md:hidden">
			{#each processes as process}
				<button class="btn" />
			{/each}
		</div>
	{/if}
</div>
