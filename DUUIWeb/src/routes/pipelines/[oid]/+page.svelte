<script lang="ts">
	import { v4 as uuidv4 } from 'uuid'
	import { goto } from '$app/navigation'
	import PipelineComponent from '$lib/components/PipelineComponent.svelte'
	import { isEqual } from 'lodash'
	import {
		faArrowDownWideShort,
		faArrowLeft,
		faArrowUpWideShort,
		faCopy,
		faFileCircleCheck,
		faFileCircleXmark,
		faFileExport,
		faGears,
		faHourglassEnd,
		faHourglassStart,
		faLayerGroup,
		faMap,
		faPlus,
		faTrash,
		faWifi
	} from '@fortawesome/free-solid-svg-icons'
	import { type ModalSettings, getModalStore } from '@skeletonlabs/skeleton'
	import { dndzone, type DndEvent } from 'svelte-dnd-action'
	import Fa from 'svelte-fa'
	import { flip } from 'svelte/animate'

	import { getToastStore } from '@skeletonlabs/skeleton'
	import type { PageServerData } from './$types'
	import { datetimeToString, equals } from '$lib/utils/text'
	import { getDuration } from '$lib/utils/time'
	import { TabGroup, Tab } from '@skeletonlabs/skeleton'
	import { page } from '$app/stores'
	import { Api, makeApiCall } from '$lib/utils/api'
	import { documentStatusNamesString, getStatusIcon, info, success } from '$lib/utils/ui'
	import { pipelineToJson } from '$lib/duui/pipeline'
	import ActionButton from '$lib/components/ActionButton.svelte'
	import type { DUUIComponent } from '$lib/duui/component'
	import { Status } from '$lib/duui/monitor'
	import type { DUUIProcess } from '$lib/duui/process'
	import { markedForDeletionStore } from '$lib/store'
	import SettingsMapper from '$lib/components/SettingsMapper.svelte'

	export let data: PageServerData
	let { pipeline, processes } = data

	pipeline.components.forEach((c) => (c.id = uuidv4()))
	pipeline.components.sort((a, b) => (a.index > b.index ? 1 : -1))

	let startingService: boolean = false
	let stoppingService: boolean = false
	let hasChanges: boolean = false
	let settings: Map<string, string>

	let name: string = pipeline.name
	let description: string = pipeline.description
	let componentsOrder: Map<number, string> = new Map(
		pipeline.components.map((c) => [c.index, c.oid])
	)

	let components: DUUIComponent[] = pipeline.components

	let tabSet: number = +($page.url.searchParams.get('tab') || 0)

	let filteredProcesses: DUUIProcess[] = []

	let sortIndex: number = 0
	let sortOrder: number = 1

	let pageIndex: number = 0
	let itemsPerPage: number = 10
	let maxPages: number = processes.length / itemsPerPage
	let sortedProcessses: DUUIProcess[] = processes
	let statusFilter: string = Status.Any

	let tableHeader: string[] = ['Started At', 'IO', '# Documents', 'Progress', 'Status', 'Duration']

	let flipDurationMs = 300

	const handleDndConsider = (event: CustomEvent<DndEvent<DUUIComponent>>) => {
		pipeline.components = event.detail.items
		pipeline.components = [...pipeline.components]
	}

	const handleDndFinalize = (event: CustomEvent<DndEvent<DUUIComponent>>) => {
		pipeline.components = event.detail.items
		pipeline.components = [...pipeline.components]
		pipeline.components.forEach(
			(c) => (c.index = pipeline.components.map((c) => c.oid).indexOf(c.oid))
		)
		console.log(pipeline.components.map((c) => c.name + ': ' + c.index))
	}

	const modalStore = getModalStore()
	const toastStore = getToastStore()

	const updatePipeline = async () => {
		pipeline.settings = Object.fromEntries(settings.entries())
		pipeline.components = pipeline.components.map((c) => {
			return { ...c, index: pipeline.components.indexOf(c) }
		})

		let response = await makeApiCall(Api.Pipelines, 'PUT', pipeline)

		if (response.ok) {
			toastStore.trigger(success('Changes saved successfully'))
			name = pipeline.name
			description = pipeline.description
			components = pipeline.components
			componentsOrder = new Map(pipeline.components.map((c) => [c.index, c.oid]))
		}

		for (let oid of $markedForDeletionStore) {
			makeApiCall(Api.Components, 'DELETE', { oid: oid })
		}
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

			let response = await makeApiCall(Api.Pipelines, 'DELETE', { oid: pipeline.oid })
			if (response.ok) {
				goto('/pipelines')
				toastStore.trigger(info('Pipeline deleted successfully'))
			}
		})
	}

	const copyPipeline = async () => {
		let response = await makeApiCall(Api.Pipelines, 'POST', pipeline)
		if (response.ok) {
			const data = await response.json()
			toastStore.trigger(info('Pipeline copied successfully'))
			goto(`/pipelines/${data.oid}`, { invalidateAll: true, replaceState: true })
		}
	}

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
			toastStore.trigger(info(`Service ${pipeline.name} has been started`))
		}

		startingService = false
	}

	const stopService = async () => {
		stoppingService = true
		toastStore.trigger(info(`Stopping Service ${pipeline.name}`))

		let response = await makeApiCall(Api.Services, 'PUT', pipeline)

		if (response.ok) {
			pipeline.serviceStartTime = 0
			toastStore.trigger(info(`Service ${pipeline.name} has been stopped`))
		}

		stoppingService = false
	}

	function exportPipeline() {
		const blob = new Blob([JSON.stringify(pipelineToJson(pipeline))], { type: 'application/json' })
		const url = URL.createObjectURL(blob)
		const anchor = document.createElement('a')
		anchor.href = url
		anchor.download = `${pipeline.name}.json`
		document.body.appendChild(anchor)
		anchor.click()
		document.body.removeChild(anchor)
		URL.revokeObjectURL(url)
	}

	const discardChanges = () => {
		pipeline.name = name
		pipeline.description = description
		pipeline.components = components
		pipeline.components.forEach(
			(c) => (c.index = pipeline.components.map((c) => c.oid).indexOf(c.oid))
		)
		componentsOrder = new Map(pipeline.components.map((c) => [c.index, c.oid]))
		settings = new Map(Object.entries(pipeline.settings))
	}

	$: {
		hasChanges =
			name !== pipeline.name ||
			description !== pipeline.description ||
			components.length != pipeline.components.length

		for (let component of pipeline.components) {
			if (component.oid !== componentsOrder.get(component.index)) {
				hasChanges = true
				break
			}
		}

		if (settings) {
			hasChanges ||= !isEqual(Object.fromEntries(settings), pipeline.settings)
		}
	}

	$: {
		if (processes.length > 0) {
			filteredProcesses = processes.filter(
				(p) => equals(p.status, statusFilter) || statusFilter === '' || statusFilter === Status.Any
			)

			sortedProcessses = filteredProcesses
				.sort((a, b) => {
					if (sortIndex === 1) return a.input.source > b.input.source ? sortOrder : -sortOrder
					if (sortIndex === 2)
						return a.documentNames.length > b.documentNames.length ? sortOrder : -sortOrder
					if (sortIndex === 3) return a.progress > b.progress ? sortOrder : -sortOrder
					if (sortIndex === 4)
						return documentStatusNamesString.indexOf(a.status) >
							documentStatusNamesString.indexOf(b.status)
							? sortOrder
							: -sortOrder

					if (sortIndex === 5)
						return getDuration(a.startTime, a.endTime) > getDuration(b.startTime, b.endTime)
							? sortOrder
							: -sortOrder
					return a.startTime > b.startTime ? -sortOrder : sortOrder
				})
				.slice(itemsPerPage * pageIndex, itemsPerPage * (pageIndex + 1))

			maxPages = Math.floor(filteredProcesses.length / itemsPerPage)
			if (filteredProcesses.length % itemsPerPage === 0) maxPages -= 1
		}
	}

	const sortTable = (index: number) => {
		sortOrder = sortIndex !== index ? 1 : (sortOrder *= -1)
		sortIndex = index
	}
</script>

<div class="container h-full flex-col mx-auto flex gap-4 md:my-8">
	<div class="flex items-end justify-between gap-4">
		<h1 class="h2">{pipeline.name}</h1>
		<div class="flex items-center justify-between">
			{#if pipeline.lastUsed}
				<p>Last used: {datetimeToString(new Date(pipeline.lastUsed))}</p>
			{:else}
				<p>Last used: Never</p>
			{/if}
		</div>
	</div>
	<hr />
	<div class="flex items-center justify-start gap-4">
		<ActionButton text="Back" icon={faArrowLeft} on:click={() => goto('/pipelines')} />
		<ActionButton text="Export" icon={faFileExport} on:click={exportPipeline} />
		<ActionButton text="Copy" icon={faCopy} on:click={copyPipeline} />
		<ActionButton text="Delete" icon={faTrash} on:click={deletePipeline} />
		<ActionButton
			text="New Run"
			icon={faPlus}
			on:click={() => goto('/process?pipeline=' + pipeline.oid)}
		/>
		{#if hasChanges}
			<ActionButton text="Save changes" icon={faFileCircleCheck} on:click={updatePipeline} />
			<ActionButton text="Discard changes" icon={faFileCircleXmark} on:click={discardChanges} />
		{/if}

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
		<TabGroup active="variant-filled-surface" border="none" rounded="rounded-md">
			<Tab bind:group={tabSet} name="settings" value={0}>
				<div class="flex items-center gap-4">
					<Fa icon={faGears} />
					<span>Settings</span>
				</div>
			</Tab>

			<Tab bind:group={tabSet} name="components" value={1}
				><div class="flex items-center gap-4">
					<Fa icon={faMap} />
					<span>Components</span>
				</div></Tab
			>
			<Tab bind:group={tabSet} name="processes" value={2}
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
			</div>
			<div class="variant-soft-surface p-4 shadow-lg rounded-md">
				<SettingsMapper bind:settings />
			</div>
		</div>
	{:else if tabSet === 1}
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
							}}
						/>
					</div>
				{/each}
			</ul>
		</div>
	{:else if tabSet === 2}
		<!-- TODO: Custom table -->
		<!-- 
		<Table
			class="md:block rounded-md shadow-lg variant-soft-surface"
			source={tableData}
			interactive
			on:selected={(event) => goto('/process/' + event.detail[0].oid)}
		/> -->

		<div class="overflow-hidden rounded-md border-[1px] border-surface-500">
			<div
				class="header grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-6 variant-soft-surface border-b-4 border-primary-500"
			>
				{#each tableHeader as column, index}
					<button
						class="btn variant-soft-surface gap-4 justify-start items-center rounded-none p-4 text-left
						 {[3].includes(index)
							? 'hidden sm:inline-flex'
							: [2].includes(index)
							? 'hidden md:inline-flex'
							: [1, 5].includes(index)
							? 'hidden lg:inline-flex'
							: ''}"
						on:click={() => sortTable(index)}
					>
						<span>{column}</span>
						{#if sortIndex === index}
							<Fa icon={sortOrder === -1 ? faArrowDownWideShort : faArrowUpWideShort} />
						{/if}
					</button>
				{/each}
			</div>

			<div class="rounded-md rounded-t-none overflow-hidden flex flex-col">
				{#each sortedProcessses as process, index}
					<button
						class="btn rounded-none {index % 2 === 1 || sortedProcessses.length === 1
							? 'variant-soft-surface'
							: ''} hover:variant-soft-primary grid-cols-2 grid sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-6 gap-8 p-4 text-left"
						on:click={() => goto('/process/' + process.oid)}
					>
						<p>{datetimeToString(new Date(process.startTime))}</p>
						<p class="hidden lg:block">{process.input.source} / {process.output.target}</p>
						<p class="hidden md:block text-center">{process.documentNames.length}</p>
						<p class="hidden sm:flex items-center gap-4">
							<span>{(process.progress / process.documentNames.length) * 100} %</span>
							<span>{process.progress} / {process.documentNames.length}</span>
						</p>
						<p class="flex justify-start items-center gap-4">
							<Fa
								icon={getStatusIcon(process.status)}
								size="lg"
								class={equals(process.status, Status.Running) ? 'animate-spin-slow' : ''}
							/>
							<span>{process.status}</span>
						</p>
						<p class="hidden lg:block text-center">
							{getDuration(process.startTime, process.endTime)}
						</p>
					</button>
				{/each}
			</div>
		</div>
	{/if}
</div>
