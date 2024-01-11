<script lang="ts">
	import { v4 as uuidv4 } from 'uuid'
	import { goto, invalidateAll } from '$app/navigation'
	import PipelineComponent from '$lib/svelte/widgets/duui/PipelineComponent.svelte'
	import { isEqual, cloneDeep } from 'lodash'
	import {
		faArrowDownWideShort,
		faArrowLeft,
		faArrowUpWideShort,
		faCopy,
		faFileCircleCheck,
		faFileCircleXmark,
		faFileExport,
		faHourglass,
		faHourglassStart,
		faPause,
		faPlay,
		faPlus,
		faRefresh,
		faTrash,
		faWifi
	} from '@fortawesome/free-solid-svg-icons'
	import { type ModalSettings, getModalStore } from '@skeletonlabs/skeleton'
	import { dndzone, type DndEvent } from 'svelte-dnd-action'
	import Fa from 'svelte-fa'
	import { flip } from 'svelte/animate'

	import { getToastStore } from '@skeletonlabs/skeleton'
	import type { PageServerData } from './$types'
	import { datetimeToString, equals } from '$lib/duui/utils/text'
	import { getDuration } from '$lib/duui/utils/time'
	import { TabGroup, Tab } from '@skeletonlabs/skeleton'
	import { page } from '$app/stores'
	import { Api, makeApiCall } from '$lib/duui/utils/api'
	import {
		documentStatusNamesString,
		getStatusIcon,
		info,
		success,
		variantError,
		variantSuccess
	} from '$lib/duui/utils/ui'
	import { pipelineToExportableJson } from '$lib/duui/pipeline'
	import ActionButton from '$lib/svelte/widgets/action/ActionButton.svelte'
	import type { DUUIComponent } from '$lib/duui/component'
	import { Status, statusNames } from '$lib/duui/monitor'
	import type { DUUIProcess } from '$lib/duui/process'
	import { markedForDeletionStore } from '$lib/store'
	import Mapper from '$lib/svelte/widgets/input/Mapper.svelte'
	import TextArea from '$lib/svelte/widgets/input/TextArea.svelte'
	import Text from '$lib/svelte/widgets/input/TextInput.svelte'
	import SpeedDial from '$lib/svelte/widgets/navigation/SpeedDial.svelte'
	import IconButton from '$lib/svelte/widgets/action/IconButton.svelte'
	import Paginator from '$lib/svelte/widgets/navigation/Paginator.svelte'
	import Select from '$lib/svelte/widgets/input/Select.svelte'
	import Anchor from '$lib/svelte/widgets/action/Anchor.svelte'
	import Chips from '$lib/svelte/widgets/input/Chips.svelte'
	import { redirect } from '@sveltejs/kit'
	import JsonPreview from '$lib/svelte/widgets/input/JsonPreview.svelte'

	export let data: PageServerData
	let { pipeline, processInfo } = data
	let { processes, count } = processInfo

	pipeline.components.forEach((c) => (c.id = uuidv4()))

	let pipelineCopy = cloneDeep(pipeline)

	const modalStore = getModalStore()
	const toastStore = getToastStore()

	let startingService: boolean = false
	let stoppingService: boolean = false

	let settings: Map<string, string> = new Map(Object.entries(pipeline.settings))
	let hasChanges: boolean = false

	let tableHeader: string[] = ['Started At', 'IO', '# Documents', 'Progress', 'Status', 'Duration']

	let sort: Sort = {
		by: 0,
		order: 1
	}

	const sortMap: Map<number, string> = new Map([
		[0, 'startTime'],
		[1, 'input'],
		[2, 'count'],
		[3, 'progress'],
		[4, 'status'],
		[5, 'duration']
	])

	let paginationSettings: PaginationSettings = {
		page: 0,
		limit: 10,
		total: count,
		sizes: [5, 10, 20, 50]
	}

	let filter: string[] = [Status.Any]
	let sortedProcessses: DUUIProcess[] = processes
	let tabSet: number = +($page.url.searchParams.get('tab') || 0)

	const handleDndConsider = (event: CustomEvent<DndEvent<DUUIComponent>>) => {
		pipeline.components = [...event.detail.items]

		if (pipeline.components.length <= 1) {
			pipelineCopy.components = cloneDeep(pipeline.components)
		}
	}

	const handleDndFinalize = (event: CustomEvent<DndEvent<DUUIComponent>>) => {
		pipeline.components = [...event.detail.items]
		pipeline.components.forEach(
			(c) => (c.index = pipeline.components.map((c) => c.oid).indexOf(c.oid))
		)

		if (pipeline.components.length <= 1) {
			pipelineCopy.components = cloneDeep(pipeline.components)
		}
	}

	const updatePipeline = async () => {
		pipeline.settings = Object.fromEntries(settings.entries())
		pipeline.components = pipeline.components.map((c) => {
			return { ...c, index: pipeline.components.indexOf(c) }
		})

		let response = await makeApiCall(Api.Pipelines, 'PUT', pipeline)

		if (response.ok) {
			toastStore.trigger(success('Changes saved successfully'))
			pipelineCopy = cloneDeep(pipeline)
		}

		for (let oid of $markedForDeletionStore) {
			makeApiCall(Api.Components, 'DELETE', { oid: oid })
		}
	}

	const deletePipeline = async () => {
		new Promise<boolean>((resolve) => {
			const modal: ModalSettings = {
				type: 'component',
				component: 'deleteModal',
				meta: {
					title: 'Delete Pipeline',
					body: `Are you sure you want to delete ${pipeline.name}?`
				},
				response: (r: boolean) => {
					resolve(r)
				}
			}

			modalStore.trigger(modal)
		}).then(async (accepted: boolean) => {
			if (!accepted) return

			const response = await fetch(`./api`, {
				method: 'DELETE',
				body: JSON.stringify({ oid: pipeline.oid })
			})

			if (response.ok) {
				goto('/pipelines')
				toastStore.trigger(info('Pipeline deleted successfully'))
			}
		})
	}

	const copyPipeline = async () => {
		new Promise<string>((resolve) => {
			const modal: ModalSettings = {
				type: 'component',
				component: 'promptModal',
				meta: {
					title: 'Copy Pipeline',
					body: `Choose a new Name`,
					value: pipeline.name + ' - Copy'
				},
				response: (r: string) => {
					resolve(r)
				}
			}
			modalStore.trigger(modal)
		}).then(async (newName: string) => {
			if (!newName) return

			const response = await fetch('./api', {
				method: 'POST',
				body: JSON.stringify({
					pipeline: {
						...pipeline,
						name: newName
					}
				})
			})

			if (response.ok) {
				const data = await response.json()
				toastStore.trigger(info('Pipeline copied successfully'))
				goto(`/pipelines/${data.oid}`, { replaceState: true })
			}
		})
	}

	const manageService = async () => {
		if (startingService || stoppingService) return
		pipeline.serviceStartTime === 0 ? startService() : stopService()
	}

	const startService = async () => {
		startingService = true
		let response = await makeApiCall(Api.Services, 'POST', pipeline)

		if (response.ok) {
			const json = await response.json()
			pipeline.serviceStartTime = json.serviceStartTime
			pipelineCopy.serviceStartTime = json.serviceStartTime
		}

		startingService = false
	}

	const stopService = async () => {
		stoppingService = true

		let response = await makeApiCall(Api.Services, 'PUT', pipeline)

		if (response.ok) {
			pipeline.serviceStartTime = 0
			pipelineCopy.serviceStartTime = 0
		}

		stoppingService = false
	}

	const exportPipeline = () => {
		const blob = new Blob([JSON.stringify(pipelineToExportableJson(pipeline))], {
			type: 'application/json'
		})
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
		pipeline = cloneDeep(pipelineCopy)
		settings = new Map(Object.entries(pipeline.settings))
	}

	const updateTable = async () => {
		const lastFilter: string | undefined = filter.at(-1)

		if (equals(lastFilter || '', Status.Any) || filter.length === 0) {
			filter = [Status.Any]
		} else {
			filter = filter.filter((status) => !equals(status, Status.Any))
		}

		const response = await fetch(
			`./api
			?pipeline_id=${pipeline.oid}
			&limit=${paginationSettings.limit}
			&skip=${paginationSettings.page * paginationSettings.limit}
			&by=${sortMap.get(sort.by)}
			&order=${sort.order}
			&filter=${filter.join(';')}`,
			{
				method: 'GET'
			}
		)

		const json = await response.json()
		const data = json.processInfo
		processes = data.processes
		count = data.count
		sortedProcessses = processes
	}

	$: {
		hasChanges = !isEqual(pipeline, pipelineCopy)

		if (settings) {
			hasChanges ||= !isEqual(Object.fromEntries(settings.entries()), pipeline.settings)
		}
	}

	const sortTable = (index: number) => {
		sort.order = sort.by !== index ? 1 : sort.order === 1 ? -1 : 1
		sort.by = index
		updateTable()
	}
</script>

<SpeedDial>
	<svelte:fragment slot="content">
		<IconButton icon={faArrowLeft} on:click={() => goto('/pipelines')} />
		<IconButton icon={faFileExport} on:click={exportPipeline} />
		<IconButton icon={faCopy} on:click={copyPipeline} />
		<IconButton icon={faTrash} on:click={deletePipeline} />
		<IconButton icon={faPlus} on:click={() => goto('/process?oid=' + pipeline.oid)} />
		{#if startingService || stoppingService}
			<IconButton icon={faRefresh} _class="lg:ml-auto col-span-2" />
		{:else if pipeline.serviceStartTime === 0}
			<IconButton icon={faPlay} on:click={manageService} _class="lg:ml-auto col-span-2 pl-1" />
		{:else}
			<IconButton
				icon={faPause}
				on:click={manageService}
				_class="lg:ml-auto col-span-2 dark:variant-soft-success pl"
			/>
		{/if}
	</svelte:fragment>
</SpeedDial>

<svelte:head>
	<title>{pipeline.name}</title>
</svelte:head>

<div class="container h-full flex-col mx-auto flex gap-4 md:my-8">
	<div class="flex items-center md:items-end justify-between gap-4">
		<h1 class="h2">{pipeline.name}</h1>
		<div class="hidden md:flex items-center justify-between gap-4">
			<p>Times used: {pipeline.timesUsed}</p>
			{#if pipeline.lastUsed}
				<p>Last used: {datetimeToString(new Date(pipeline.lastUsed))}</p>
			{:else}
				<p>Last used: Never</p>
			{/if}
		</div>
		{#if pipeline.serviceStartTime !== 0}
			<Fa icon={faWifi} class="md:hidden text-success-400" />
		{/if}
	</div>
	<hr class="bg-surface-400/20 h-[1px] !border-0 rounded" />
	<div class="hidden md:grid grid-cols-2 md:grid-cols-3 lg:flex items-center justify-start gap-4">
		<Anchor text="Back" icon={faArrowLeft} leftToRight={true} href="/pipelines" />
		<ActionButton text="Export" icon={faFileExport} on:click={exportPipeline} />
		<ActionButton text="Copy" icon={faCopy} on:click={copyPipeline} />
		<ActionButton text="Delete" icon={faTrash} on:click={deletePipeline} />

		{#if startingService}
			<ActionButton
				icon={faHourglassStart}
				text="Starting Service"
				_class="lg:ml-auto col-span-2"
			/>
		{:else if stoppingService}
			<ActionButton
				icon={faHourglass}
				text="Stopping Service"
				_class="lg:ml-auto col-span-2 animate-pulse"
			/>
		{:else if pipeline.serviceStartTime === 0}
			<ActionButton
				icon={faPlay}
				text="Start Service"
				on:click={manageService}
				_class="lg:ml-auto col-span-2"
			/>
		{:else}
			<ActionButton
				icon={faPause}
				text="Stop Service"
				on:click={manageService}
				_class="lg:ml-auto col-span-2"
			/>
		{/if}
	</div>

	<div class="section-wrapper text-xs md:text-base">
		<TabGroup
			active="dark:variant-soft-primary variant-filled-primary"
			border="border-none"
			rounded="rounded-md"
			justify="grid grid-cols-3"
			hover="bg-fancy"
		>
			<Tab bind:group={tabSet} name="settings" value={0}>Settings</Tab>
			<Tab bind:group={tabSet} name="components" value={1}>Components</Tab>
			<Tab bind:group={tabSet} name="processes" value={2}>Processes</Tab>
		</TabGroup>
	</div>

	{#if tabSet === 0}
		<div class=" grid md:grid-cols-2 gap-4">
			<div class="space-y-4 section-wrapper p-4">
				<Text label="Name" name="pipeline-name" bind:value={pipeline.name} />
				<TextArea
					label="Description"
					name="pipeline-description"
					bind:value={pipeline.description}
				/>
				<Chips label="Tags" placeholder="Add a tag..." bind:values={pipeline.tags} />
			</div>
			<div class="section-wrapper p-2 px-4">
				<JsonPreview bind:data={settings} />
				<!-- <Mapper
					bind:map={settings}
					on:update={(event) => {
						pipeline.settings = Object.fromEntries(event.detail.map.entries())
					}}
				/> -->
				<!-- <pre>{JSON.stringify(pipeline, null, 2)}</pre> -->
			</div>
		</div>
	{:else if tabSet === 1}
		<div
			class="rounded-md border border-surface-200
				   dark:border-surface-500 isolate container space-y-8 bg-surface-100 dark:variant-soft-surface mx-auto shadow-lg p-4 md:p-16"
		>
			<ul
				use:dndzone={{ items: pipeline.components, dropTargetStyle: {} }}
				on:consider={(event) => handleDndConsider(event)}
				on:finalize={(event) => handleDndFinalize(event)}
				class="grid gap-4 max-w-5xl mx-auto"
			>
				{#each pipeline.components as component (component.id)}
					<div animate:flip={{ duration: 300 }}>
						<PipelineComponent
							{component}
							on:update={updatePipeline}
							on:deleteComponent={(event) => {
								pipeline.components = pipeline.components.filter((c) => c.oid !== event.detail.oid)
								pipelineCopy = cloneDeep(pipeline)
							}}
						/>
					</div>
				{/each}
			</ul>
		</div>
	{:else if tabSet === 2}
		<div class="grid md:flex items-center gap-4 justify-between">
			<ActionButton
				text="New Process"
				icon={faPlus}
				_class="mr-auto hidden md:inline-flex"
				on:click={() => goto('/process?oid=' + pipeline.oid)}
			/>
			<Select
				on:change={updateTable}
				label="Status"
				name="Status"
				bind:selected={filter}
				options={statusNames}
			/>
		</div>
		<div class="section-wrapper text-xs">
			<div
				class="header grid grid-cols-3 md:grid-cols-4 lg:grid-cols-6 bg-surface-100 dark:variant-soft-surface border-b-4 border-primary-500"
			>
				{#each tableHeader as column, index}
					<button
						class="btn-sm md:text-base md:inline-flex px-4 bg-fancy
						 	   dark:variant-soft-surface gap-4 justify-start items-center rounded-none p-3 text-left
						 {[2].includes(index)
							? 'hidden md:inline-flex'
							: [1, 5].includes(index)
							? 'hidden lg:inline-flex'
							: ''}"
						on:click={() => sortTable(index)}
					>
						<span>{column}</span>
						{#if sort.by === index}
							<Fa icon={sort.order === -1 ? faArrowDownWideShort : faArrowUpWideShort} />
						{/if}
					</button>
				{/each}
			</div>

			<div class=" overflow-hidden flex flex-col">
				{#each sortedProcessses as process}
					<a
						href={`/process/${process.oid}?limit=10&skip=0`}
						class="rounded-none first:border-t-0 border-t-[1px]
						 dark:border-t-surface-500 dark:hover:variant-soft-primary hover:variant-filled-primary
						 grid-cols-3 grid md:grid-cols-4 lg:grid-cols-6 gap-8 p-3 px-4 text-left text-xs md:text-sm"
					>
						<p>{datetimeToString(new Date(process.startTime))}</p>
						<p class="hidden lg:block">{process.input.source} / {process.output.target}</p>
						<p class="hidden md:block">{process.documentNames.length}</p>
						<div class="md:grid md:grid-cols-2 items-center justify-start md:gap-4">
							<p>
								{((process.progress / process.documentNames.length) * 100 || 0).toFixed(2)} %
							</p>
							<p>{process.progress} / {process.documentNames.length}</p>
						</div>
						<p class="flex justify-start items-center gap-2 md:gap-4">
							<Fa
								icon={getStatusIcon(process.status)}
								size="lg"
								class={equals(process.status, Status.Running) ? 'animate-spin-slow' : ''}
							/>
							<span>{process.status}</span>
						</p>
						<p class="hidden lg:block">
							{getDuration(process.startTime, process.endTime)}
						</p>
					</a>
				{/each}
			</div>
		</div>
		<Paginator bind:settings={paginationSettings} on:change={updateTable} />
	{/if}

	{#if hasChanges}
		<div class="flex items-center gap-4">
			<ActionButton
				text="Save changes"
				icon={faFileCircleCheck}
				on:click={updatePipeline}
				variant={variantSuccess}
			/>
			<ActionButton
				variant={variantError}
				text="Discard changes"
				icon={faFileCircleXmark}
				on:click={discardChanges}
			/>
		</div>
	{/if}
</div>
