<script lang="ts">
	import { goto } from '$app/navigation'
	import PipelineComponent from '$lib/svelte/widgets/duui/PipelineComponent.svelte'
	import {
		faArrowDownWideShort,
		faArrowLeft,
		faArrowUpWideShort,
		faFileCircleCheck,
		faFileCircleXmark,
		faFileClipboard,
		faFileExport,
		faGear,
		faGears,
		faMap,
		faPause,
		faPlay,
		faPlus,
		faRotate,
		faTrash,
		faWifi
	} from '@fortawesome/free-solid-svg-icons'
	import { getModalStore, TabAnchor, type ModalSettings } from '@skeletonlabs/skeleton'
	import { cloneDeep, isEqual } from 'lodash'
	import { dndzone, type DndEvent } from 'svelte-dnd-action'
	import Fa from 'svelte-fa'
	import { flip } from 'svelte/animate'
	import { v4 as uuidv4 } from 'uuid'

	import { page } from '$app/stores'
	import type { DUUIComponent } from '$lib/duui/component'
	import { Status, statusNames } from '$lib/duui/monitor'
	import { pipelineToExportableJson } from '$lib/duui/pipeline'
	import type { DUUIProcess } from '$lib/duui/process'
	import { Api, makeApiCall } from '$lib/duui/utils/api'
	import { datetimeToString, equals } from '$lib/duui/utils/text'
	import { getDuration } from '$lib/duui/utils/time'
	import { getStatusIcon, info, success, variantError, variantSuccess } from '$lib/duui/utils/ui'
	import { markedForDeletionStore } from '$lib/store'
	import ActionButton from '$lib/svelte/widgets/action/ActionButton.svelte'
	import Chips from '$lib/svelte/widgets/input/Chips.svelte'
	import JsonPreview from '$lib/svelte/widgets/input/JsonPreview.svelte'
	import Select from '$lib/svelte/widgets/input/Select.svelte'
	import TextArea from '$lib/svelte/widgets/input/TextArea.svelte'
	import Text from '$lib/svelte/widgets/input/TextInput.svelte'
	import Paginator from '$lib/svelte/widgets/navigation/Paginator.svelte'
	import { Tab, TabGroup, getToastStore } from '@skeletonlabs/skeleton'
	import { onDestroy, onMount } from 'svelte'
	import type { PageServerData } from './$types'
	import Mapper from '$lib/svelte/widgets/input/Mapper.svelte'

	export let data: PageServerData
	let { pipeline, processInfo } = data
	let { processes, count } = processInfo

	pipeline.components.forEach((c) => (c.id = uuidv4()))

	let pipelineCopy = cloneDeep(pipeline)

	const modalStore = getModalStore()
	const toastStore = getToastStore()

	let settings: Map<string, string> = new Map(Object.entries(pipeline.settings || {}))

	let hasChanges: boolean = false

	let tableHeader: string[] = ['Started At', 'IO', '# Documents', 'Progress', 'Status', 'Duration']

	let sort: Sort = {
		by: 0,
		order: 1
	}

	let interval: NodeJS.Timeout

	const update = async () => {
		const fetchUpdate = async () => {
			const response = await fetch(`/pipelines/api/update?id=${pipeline.oid}`, {
				method: 'GET'
			})

			const json = await response.json()

			pipeline.serviceStartTime = json.serviceStartTime
			pipeline.state = json.state
		}
		interval = setInterval(fetchUpdate, 500)
		fetchUpdate()
	}

	onDestroy(() => {
		clearInterval(interval)
	})

	onMount(() => {
		return () => clearInterval(interval)
	})

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
					message: `Choose a new Name`,
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
		if (pipeline.state === Status.Setup || pipeline.state === Status.Shutdown) {
			return
		}
		update()
		pipeline.state === Status.Inactive ? startService() : stopService()
	}

	const startService = async () => {
		const response = await makeApiCall(Api.Services, 'POST', pipeline)

		if (response.ok) {
			pipeline.state = Status.Idle
			clearInterval(interval)
		}
	}

	const stopService = async () => {
		const response = await makeApiCall(Api.Services, 'PUT', pipeline)

		if (response.ok) {
			pipeline.state = Status.Inactive
			clearInterval(interval)
		}
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
		hasChanges = !isEqual(
			{
				name: pipeline.name,
				description: pipeline.description,
				tags: pipeline.tags,
				settings: pipeline.settings,
				components: pipeline.components
			},
			{
				name: pipelineCopy.name,
				description: pipelineCopy.description,
				tags: pipelineCopy.tags,
				settings: pipelineCopy.settings,
				components: pipelineCopy.components
			}
		)

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

<!-- 
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
</SpeedDial> -->

<svelte:head>
	<title>{pipeline.name}</title>
</svelte:head>

<div class="h-full">
	<div class="grid">
		<div
			class="page-wrapper bg-solid md:top-0 z-10 left-0 bottom-0 right-0 row-start-2 fixed md:sticky md:row-start-1"
		>
			<div class="grid grid-cols-5 md:flex items-center md:justify-start gap-4 relative">
				<a href="/pipelines">
					<button class="button-primary" on:click={() => goto('/pipelines')}>
						<Fa icon={faArrowLeft} />
						<span class="hidden md:inline">Pipelines</span>
					</button>
				</a>

				<button class="button-primary" on:click={exportPipeline}>
					<Fa icon={faFileExport} />
					<span class="hidden md:inline">Export</span>
				</button>

				<button class="button-primary" on:click={copyPipeline}>
					<Fa icon={faFileClipboard} />
					<span class="hidden md:inline">Copy</span>
				</button>

				<button
					class="button-primary md:ml-auto"
					on:click={manageService}
					disabled={pipeline.state === Status.Setup || pipeline.state === Status.Shutdown}
				>
					{#if pipeline.state === Status.Setup || pipeline.state === Status.Shutdown}
						<Fa icon={faRotate} spin />
					{:else}
						<Fa icon={pipeline.state === Status.Idle ? faPause : faPlay} />
					{/if}
					<span class="hidden md:inline"
						>{pipeline.state === Status.Inactive
							? 'Setup'
							: pipeline.state === Status.Idle
							? 'Shutdown'
							: 'Processing'}</span
					>
				</button>

				<button class="button-error" on:click={deletePipeline}>
					<Fa icon={faTrash} />
					<span class="hidden md:inline">Delete</span>
				</button>
			</div>
		</div>
		<div class="p-4 md:p-8">
			<h1 class="h1 mb-8">{pipeline.name}</h1>

			<div class="text-xs md:text-base flex">
				<TabGroup
					regionList="border-none dark:bg-transparent"
					active="!border-b-0 section-wrapper text-black dark:text-surface-100"
					rounded="rounded-md !rounded-b-none"
					hover="hover:bg-surface-200/20 dark:hover:bg-surface-900/70"
				>
					<Tab padding="px-8 py-3" bind:group={tabSet} name="settings" value={0}>
						<span> Settings </span>
					</Tab>

					<Tab padding="px-8 py-3" bind:group={tabSet} name="processes" value={1}>
						<span> Processes </span>
					</Tab>
				</TabGroup>
				{#if tabSet === 1}
					<div
						class="ml-auto flex overflow-hidden justify-between section-wrapper !shadow-none !border-b-0 !rounded-b-none z-10"
					>
						<button
							class="inline-flex gap-4 items-center px-4 bg-fancy"
							on:click={() => goto('/process?oid=' + pipeline.oid)}
						>
							<Fa icon={faPlus} />
							<span class="hidden md:inline">New Process</span>
						</button>

						<Select
							on:change={updateTable}
							style="z-50 !rounded-none hidden md:flex"
							border="border-l border-color"
							label="Status"
							name="Status"
							bind:selected={filter}
							options={statusNames}
						/>
					</div>
				{/if}
			</div>

			{#if tabSet === 0}
				<div class="section-wrapper !rounded-tl-none !border-t-none grid md:grid-cols-2 gap-8 p-4">
					<div class="space-y-4">
						<Text label="Name" name="pipeline-name" bind:value={pipeline.name} />
						<TextArea
							label="Description"
							name="pipeline-description"
							bind:value={pipeline.description}
						/>

						<Chips label="Tags" placeholder="Add a tag..." bind:values={pipeline.tags} />
					</div>
					<JsonPreview bind:data={settings} />
					<div class="md:col-span-2 space-y-4">
						<h2 class="h2">Components</h2>
						<div
							class="rounded-md border border-surface-200 space-y-8
								dark:border-surface-500 isolate bg-surface-100 dark:variant-soft-surface shadow-lg p-4 md:p-16"
						>
							<ul
								use:dndzone={{ items: pipeline.components, dropTargetStyle: {} }}
								on:consider={(event) => handleDndConsider(event)}
								on:finalize={(event) => handleDndFinalize(event)}
								class="grid gap-4 md:max-w-5xl mx-auto"
							>
								{#each pipeline.components as component (component.id)}
									<div animate:flip={{ duration: 300 }}>
										<PipelineComponent
											{component}
											on:update={updatePipeline}
											on:deleteComponent={(event) => {
												pipeline.components = pipeline.components.filter(
													(c) => c.oid !== event.detail.oid
												)
												pipelineCopy = cloneDeep(pipeline)
											}}
										/>
									</div>
								{/each}
							</ul>
						</div>
					</div>
				</div>
			{:else if tabSet === 1}
				<div class="section-wrapper !border-t-none gap-4 md:gap-y-8 !rounded-tr-none">
					<div class="text-xs">
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
						<div>
							<div class=" overflow-hidden flex flex-col border-b border-color">
								{#each sortedProcessses as process}
									<a
										href={`/process/${process.oid}?limit=10&skip=0`}
										class="rounded-none first:border-t-0 border-t-[1px]
						 dark:border-t-surface-500 dark:hover:variant-soft-primary hover:variant-filled-primary
						 grid-cols-3 grid md:grid-cols-4 lg:grid-cols-6 gap-8 p-3 px-4 text-left text-xs md:text-sm"
									>
										<p>{datetimeToString(new Date(process.startTime))}</p>
										<p class="hidden lg:block">
											{process.input.provider} / {process.output.provider}
										</p>
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
												class={equals(process.status, Status.Active) ? 'animate-spin-slow' : ''}
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
					</div>
					<div class="p-4">
						<Paginator bind:settings={paginationSettings} on:change={updateTable} />
					</div>
				</div>
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
	</div>
</div>
