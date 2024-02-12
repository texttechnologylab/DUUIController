<script lang="ts">
	import { goto } from '$app/navigation'
	import PipelineComponent from '$lib/svelte/components/PipelineComponent.svelte'
	import {
		faArrowDownWideShort,
		faArrowLeft,
		faArrowUpWideShort,
		faFileCircleCheck,
		faFileClipboard,
		faFileExport,
		faPause,
		faPlay,
		faPlus,
		faRocket,
		faRotate,
		faTrash
	} from '@fortawesome/free-solid-svg-icons'
	import {
		getDrawerStore,
		getModalStore,
		type DrawerSettings,
		type ModalSettings
	} from '@skeletonlabs/skeleton'

	import { dndzone, type DndEvent } from 'svelte-dnd-action'
	import Fa from 'svelte-fa'
	import { flip } from 'svelte/animate'
	import { v4 as uuidv4 } from 'uuid'

	import { page } from '$app/stores'
	import { blankComponent, type DUUIComponent } from '$lib/duui/component'
	import { PROCESS_STATUS_NAMES, Status } from '$lib/duui/monitor'
	import { pipelineToJson } from '$lib/duui/pipeline'
	import type { DUUIProcess } from '$lib/duui/process'
	import { datetimeToString, equals } from '$lib/duui/utils/text'
	import { getDuration } from '$lib/duui/utils/time'
	import {
		errorToast,
		getStatusIcon,
		infoToast,
		scrollIntoView,
		successToast
	} from '$lib/duui/utils/ui'
	import { currentPipelineStore, isDarkModeStore } from '$lib/store'
	import Chips from '$lib/svelte/components/Chips.svelte'
	import JsonInput from '$lib/svelte/components/JsonInput.svelte'
	import Paginator from '$lib/svelte/components/Paginator.svelte'
	import Select from '$lib/svelte/components/Select.svelte'
	import TextArea from '$lib/svelte/components/TextArea.svelte'
	import Text from '$lib/svelte/components/TextInput.svelte'
	import { getToastStore, Tab, TabGroup } from '@skeletonlabs/skeleton'
	import type { PageServerData } from './$types'

	import { IO_INPUT, IO_OUTPUT } from '$lib/duui/io'
	import ButtonMenu from '$lib/svelte/components/ButtonMenu.svelte'
	import { showConfirmationModal } from '$lib/svelte/utils/modal'
	import { onMount } from 'svelte'
	import {
		getErrorsPlotOptions,
		getIOPlotOptions,
		getStatusPlotOptions,
		getUsagePlotOptions
	} from './charts'
	import { getFilterOrGeneric } from '$lib/utils'

	const modalStore = getModalStore()
	const toastStore = getToastStore()

	export let data: PageServerData
	let { pipeline, processInfo } = data
	let { processes, count } = processInfo

	$currentPipelineStore = pipeline
	$currentPipelineStore.components.forEach((c) => (c.id = uuidv4()))

	let settings: Map<string, string> = new Map(Object.entries($currentPipelineStore.settings || {}))

	const tableHeader: string[] = [
		'Started At',
		'Input',
		'Output',
		'# Documents',
		'Progress',
		'Status',
		'Duration'
	]

	const sort: Sort = {
		by: 0,
		order: -1
	}

	const sortMap: Map<number, string> = new Map([
		[0, 'started_at'],
		[1, 'input.provider'],
		[2, 'output.provider'],
		[3, 'count'],
		[4, 'progress'],
		[5, 'status'],
		[6, 'duration']
	])

	let paginationSettings: PaginationSettings = {
		page: 0,
		limit: 10,
		total: count,
		sizes: [5, 10, 20, 50]
	}

	let statusFilter: string[] = [Status.Any]
	let inputFilter: string[] = ['Any']
	let outputFilter: string[] = ['Any']

	let sortedProcessses: DUUIProcess[] = processes
	let tabSet: number = +($page.url.searchParams.get('tab') || 0)

	const handleDndConsider = (event: CustomEvent<DndEvent<DUUIComponent>>) => {
		$currentPipelineStore.components = [...event.detail.items]
	}

	const handleDndFinalize = (event: CustomEvent<DndEvent<DUUIComponent>>) => {
		$currentPipelineStore.components = [...event.detail.items]
		$currentPipelineStore.components.forEach(
			(c: { index: any; oid: any }) =>
				(c.index = $currentPipelineStore.components.map((c: DUUIComponent) => c.oid).indexOf(c.oid))
		)
	}

	const updatePipeline = async () => {
		$currentPipelineStore.settings = Object.fromEntries(settings.entries())
		$currentPipelineStore.components = $currentPipelineStore.components.map((c: DUUIComponent) => {
			return { ...c, index: $currentPipelineStore.components.indexOf(c) }
		})

		let response = await fetch('/api/pipelines', {
			method: 'PUT',
			body: JSON.stringify($currentPipelineStore)
		})

		if (response.ok) {
			toastStore.trigger(successToast('Changes saved successfully'))
		}
	}

	const deletePipeline = async () => {
		const confirm = await showConfirmationModal(
			{
				title: 'Delete Pipeline',
				message: `Are you sure you want to delete ${$currentPipelineStore.name}?`,
				textYes: 'Delete'
			},
			modalStore
		)

		if (!confirm) return

		const response = await fetch(`/api/pipelines`, {
			method: 'DELETE',
			body: JSON.stringify({ oid: $currentPipelineStore.oid })
		})

		if (response.ok) {
			goto('/pipelines')
			toastStore.trigger(infoToast('Pipeline deleted successfully'))
		} else {
			toastStore.trigger(errorToast('Error: ' + response.statusText))
		}
	}

	const copyPipeline = async () => {
		new Promise<string>((resolve) => {
			const modal: ModalSettings = {
				type: 'component',
				component: 'promptModal',
				meta: {
					title: 'Copy Pipeline',
					message: `Choose a new Name`,
					value: $currentPipelineStore.name + ' - Copy'
				},
				response: (r: string) => {
					resolve(r)
				}
			}
			modalStore.trigger(modal)
		}).then(async (newName: string) => {
			if (!newName) return

			const response = await fetch('/api/pipelines', {
				method: 'POST',
				body: JSON.stringify({
					...$currentPipelineStore,
					name: newName
				})
			})

			if (response.ok) {
				const data = await response.json()
				toastStore.trigger(infoToast('Pipeline copied successfully'))
				goto(`/pipelines?id=${data.oid}`)
			} else {
			}
		})
	}

	const manageService = async () => {
		if (
			$currentPipelineStore.status === Status.Setup ||
			$currentPipelineStore.status === Status.Shutdown
		) {
			return
		}

		$currentPipelineStore.status === Status.Inactive ? startService() : stopService()
	}

	const startService = async () => {
		$currentPipelineStore.status = Status.Setup
		const response = await fetch('/api/pipelines/services', {
			method: 'POST',
			body: JSON.stringify($currentPipelineStore)
		})

		if (response.ok) {
			const result = await response.json()
			$currentPipelineStore.status = result.status
		} else {
			$currentPipelineStore.status = Status.Inactive
		}
	}

	const stopService = async () => {
		$currentPipelineStore.status = Status.Shutdown

		const response = await fetch('/api/pipelines/services', {
			method: 'PUT',
			body: JSON.stringify($currentPipelineStore)
		})

		if (response.ok) {
			const result = await response.json()
			$currentPipelineStore.status = result.status
		} else if (response.status === 404) {
			$currentPipelineStore.status = Status.Inactive
		} else {
			$currentPipelineStore.status = Status.Idle
		}
	}

	const exportPipeline = () => {
		const blob = new Blob([JSON.stringify(pipelineToJson($currentPipelineStore))], {
			type: 'application/json'
		})
		const url = URL.createObjectURL(blob)
		const anchor = document.createElement('a')
		anchor.href = url
		anchor.download = `${$currentPipelineStore.name}.json`
		document.body.appendChild(anchor)
		anchor.click()
		document.body.removeChild(anchor)
		URL.revokeObjectURL(url)
	}

	const updateTable = async () => {
		statusFilter = getFilterOrGeneric(statusFilter)
		inputFilter = getFilterOrGeneric(inputFilter)
		outputFilter = getFilterOrGeneric(outputFilter)

		const response = await fetch(
			`/api/processes/batch
			?pipeline_id=${$currentPipelineStore.oid}
			&limit=${paginationSettings.limit}
			&skip=${paginationSettings.page * paginationSettings.limit}
			&sort=${sortMap.get(sort.by)}
			&order=${sort.order}
			&status=${statusFilter.join(';')}
			&input=${inputFilter.join(';')}
			&output=${outputFilter.join(';')}`,
			{
				method: 'GET'
			}
		)

		const json = await response.json()
		const data = json.processInfo
		processes = data.processes
		count = data.count
		paginationSettings.total = count

		sortedProcessses = processes
	}
	const drawerStore = getDrawerStore()
	const addDrawer: DrawerSettings = {
		id: 'component',
		width: 'w-full sm:w-1/2',
		position: 'right',
		rounded: 'rounded-none',
		meta: {
			component: blankComponent($currentPipelineStore.oid, $currentPipelineStore.components.length),
			inEditor: false,
			creating: true
		}
	}

	const addComponent = () => {
		drawerStore.open(addDrawer)
	}

	const cloneComponent = ({ component }) => {
		drawerStore.open({
			id: 'component',
			width: 'w-full sm:w-1/2',
			position: 'right',
			rounded: 'rounded-none',
			meta: {
				component: {
					...component,
					name: component.name + ' - Copy',
					index: $currentPipelineStore.components.length
				},
				inEditor: false,
				creating: true
			}
		})
	}

	const sortTable = (index: number) => {
		sort.order = sort.by !== index ? 1 : sort.order === 1 ? -1 : 1
		sort.by = index
		updateTable()
	}

	let ApexCharts
	let loaded: boolean = false

	const chart = (node: HTMLDivElement, options: any) => {
		if (!loaded) return

		let _chart = new ApexCharts(node, options)
		_chart.render()

		return {
			update(options: any) {
				_chart.updateOptions(options)
			},
			destroy() {
				_chart.destroy()
			}
		}
	}

	let statusPlotOptions
	let errorsPlotOptions
	let ioPlotOptions
	let usagePlotOptions

	$: {
		statusPlotOptions = getStatusPlotOptions(pipeline, $isDarkModeStore)
		errorsPlotOptions = getErrorsPlotOptions(pipeline, $isDarkModeStore)
		ioPlotOptions = getIOPlotOptions(pipeline, $isDarkModeStore)
		usagePlotOptions = getUsagePlotOptions(pipeline, $isDarkModeStore)
	}

	onMount(() => {
		scrollIntoView('scroll-top')
		async function loadApexCharts() {
			const module = await import('apexcharts')
			ApexCharts = module.default
			window.ApexCharts = ApexCharts
			loaded = true
		}

		loadApexCharts()
	})

	let isInstantiating: boolean = false

	$: {
		if ($currentPipelineStore) {
			isInstantiating =
				$currentPipelineStore.status === Status.Setup ||
				$currentPipelineStore.status === Status.Shutdown
		}

		switch (tabSet) {
			case 0:
				$page.url.searchParams.set('tab', '0')
				break
			case 1:
				$page.url.searchParams.set('tab', '1')
				break
			case 2:
				$page.url.searchParams.set('tab', '2')
				break
		}
	}
</script>

<svelte:head>
	<title>{$currentPipelineStore.name}</title>
</svelte:head>

<div class="menu-mobile-lg">
	<a href="/pipelines" class="button-mobile">
		<Fa icon={faArrowLeft} />
		<span>Pipelines</span>
	</a>

	{#if $currentPipelineStore.user_id !== null}
		<a class="button-mobile" href={`/processes?pipeline_id=${$currentPipelineStore.oid}`}>
			<Fa icon={faRocket} />
			<span>Process</span>
		</a>
	{/if}

	<button class="button-mobile" on:click={updatePipeline}>
		<Fa icon={faFileCircleCheck} />
		<span>Update</span>
	</button>

	<button class="button-mobile" on:click={deletePipeline}>
		<Fa icon={faTrash} />
		<span>Delete</span>
	</button>

	<ButtonMenu>
		<button class="button" on:click={copyPipeline}>
			<Fa icon={faFileClipboard} />
			<span>Copy</span>
		</button>
		<button class="button" on:click={exportPipeline}>
			<Fa icon={faFileExport} />
			<span>Export</span>
		</button>
		<button class="button" on:click={manageService} disabled={isInstantiating}>
			{#if isInstantiating}
				<Fa icon={faRotate} spin />
				<span>Waiting</span>
			{:else}
				<Fa icon={$currentPipelineStore.status === Status.Idle ? faPause : faPlay} />
				<span>{$currentPipelineStore.status === Status.Inactive ? 'Instantiate' : 'Shutdown'}</span>
			{/if}
		</button>
	</ButtonMenu>
</div>

<div class="h-full">
	<div class="sticky top-0 bg-surface-50-900-token border-y p-4 border-color hidden lg:block z-10">
		<div class=" flex items-center justify-start relative md:gap-4">
			<a href="/pipelines" class="button-primary">
				<Fa icon={faArrowLeft} />
				<span class="text-xs md:text-base">Pipelines</span>
			</a>

			<button class="button-primary" on:click={exportPipeline}>
				<Fa icon={faFileExport} />
				<span class="text-xs md:text-base">Export</span>
			</button>

			<button class="button-primary" on:click={copyPipeline}>
				<Fa icon={faFileClipboard} />
				<span class="text-xs md:text-base">Copy</span>
			</button>
			{#if $currentPipelineStore.user_id !== null && tabSet !== 2}
				<a class="button-primary" href={`/processes?pipeline_id=${$currentPipelineStore.oid}`}>
					<Fa icon={faRocket} />
					<span>Process</span>
				</a>
			{/if}

			<button
				class="button-primary {$currentPipelineStore.status === Status.Setup ||
				$currentPipelineStore.status === Status.Shutdown
					? 'aspect-square !px-4'
					: ''}"
				on:click={manageService}
				disabled={$currentPipelineStore.status === Status.Setup ||
					$currentPipelineStore.status === Status.Shutdown}
			>
				{#if $currentPipelineStore.status === Status.Setup || $currentPipelineStore.status === Status.Shutdown}
					<Fa icon={faRotate} spin />
				{:else}
					<Fa icon={$currentPipelineStore.status === Status.Idle ? faPause : faPlay} />
					<span class="text-xs md:text-base"
						>{$currentPipelineStore.status === Status.Inactive ? 'Instantiate' : 'Shutdown'}</span
					>
				{/if}
			</button>

			<button class="button-success md:ml-auto" on:click={updatePipeline}>
				<Fa icon={faFileCircleCheck} />
				<span class="text-xs md:text-base">Update</span>
			</button>

			<button class="button-error" on:click={deletePipeline}>
				<Fa icon={faTrash} />
				<span class="text-xs md:text-base">Delete</span>
			</button>
		</div>
	</div>
	<div class="p-4 mb-32">
		<div class="text-xs md:text-base flex">
			<TabGroup
				regionList="border-none"
				active="!border-b-0 section-wrapper"
				rounded="rounded-md !rounded-b-none"
				hover="hover:bg-surface-200/20 dark:hover:bg-surface-900/70"
			>
				<Tab padding="px-6 py-3" bind:group={tabSet} name="settings" value={0}>
					<span> Settings </span>
				</Tab>

				<Tab padding="px-6 py-3" bind:group={tabSet} name="processes" value={1}>
					<span> Processes </span>
				</Tab>
				<Tab padding="px-8 py-3" bind:group={tabSet} name="statistics" value={2}>
					<span> Statistics </span>
				</Tab>
			</TabGroup>
			{#if tabSet === 1}
				<div
					class="hidden ml-auto sm:flex overflow-hidden justify-between section-wrapper !shadow-none !border-b-0 !rounded-b-none z-[5]"
				>
					<Select
						on:change={updateTable}
						style="z-50 !rounded-none hidden sm:flex px-8"
						border="border-none"
						label="Status"
						name="Status"
						bind:selected={statusFilter}
						options={PROCESS_STATUS_NAMES}
					/>

					<Select
						on:change={updateTable}
						style="z-50 !rounded-none hidden sm:flex px-8"
						border="border-none"
						label="Input"
						name="input"
						bind:selected={inputFilter}
						options={['Any'].concat(IO_INPUT)}
					/>

					<Select
						on:change={updateTable}
						style="z-50 !rounded-none hidden sm:flex px-8"
						border="border-none"
						label="Output"
						name="output"
						bind:selected={outputFilter}
						options={['Any'].concat(IO_OUTPUT)}
					/>
				</div>
			{/if}
		</div>

		{#if tabSet === 0}
			<div
				class="section-wrapper !rounded-tr-none !rounded-tl-none !border-t-none grid lg:grid-cols-2 gap-8 p-4"
			>
				<div class="space-y-4">
					<Text label="Name" name="pipeline-name" bind:value={$currentPipelineStore.name} />
					<TextArea
						label="Description"
						name="pipeline-description"
						bind:value={$currentPipelineStore.description}
					/>

					<Chips label="Tags" placeholder="Add a tag..." bind:values={$currentPipelineStore.tags} />
				</div>
				<JsonInput bind:data={settings} label="Settings" />
				<div class="lg:col-span-2 space-y-4">
					<h2 class="h2">Components</h2>
					<div
						class="min-h-[400px] rounded-md md:border border-surface-200 space-y-8
								dark:border-surface-500 isolate md:bg-surface-100 dark:md:variant-soft-surface md:shadow-lg md:p-16"
					>
						<ul
							use:dndzone={{ items: $currentPipelineStore.components, dropTargetStyle: {} }}
							on:consider={(event) => handleDndConsider(event)}
							on:finalize={(event) => handleDndFinalize(event)}
							class="grid gap-8 md:max-w-5xl mx-auto !cursor-move"
						>
							{#each $currentPipelineStore.components as component (component.id)}
								<div animate:flip={{ duration: 300 }} class="relative !cursor-move">
									<PipelineComponent
										{component}
										cloneable={true}
										on:clone={(event) => cloneComponent(event.detail)}
										on:deleteComponent={(event) => {
											$currentPipelineStore.components = $currentPipelineStore.components.filter(
												(c) => c.oid !== event.detail.oid
											)
										}}
									/>
								</div>
							{/each}
						</ul>
						<div class="mx-auto flex items-center justify-center">
							<button class="button-primary" on:click={addComponent}>
								<Fa icon={faPlus} />
								<span>Add</span>
							</button>
						</div>
					</div>
				</div>
			</div>
		{:else if tabSet === 1}
			<div class="section-wrapper !border-t-none gap-4 md:gap-y-8 !rounded-tr-none">
				<div class="text-xs">
					<div
						class="grid grid-cols-3 lg:grid-cols-7 bg-surface-100-800-token border-b border-color"
					>
						{#each tableHeader as column, index}
							<button
								class="button-neutral border-none !rounded-none"
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
									href={`/processes/${process.oid}?limit=20&skip=0`}
									class="rounded-none
											first:border-t-0 border-t border-color
										    grid grid-cols-3 lg:grid-cols-7 gap-8 items-center
											px-4 py-3
											hover:variant-filled-primary
											text-xs lg:text-sm"
								>
									<p>{datetimeToString(new Date(process.started_at))}</p>
									<p>
										{process.input.provider}
									</p>
									<p>
										{process.output.provider}
									</p>
									<p>{process.document_names.length}</p>
									<p>
										{((process.progress / process.document_names.length) * 100 || 0).toFixed(2)}
										%
									</p>
									<p class="flex justify-start items-center gap-2 md:gap-4">
										<Fa
											icon={getStatusIcon(process.status)}
											size="lg"
											class={equals(process.status, Status.Active) ? 'animate-spin-slow' : ''}
										/>
										<span>{process.status}</span>
									</p>
									<p>
										{getDuration(process.started_at, process.finished_at)}
									</p>
								</a>
							{/each}
						</div>
					</div>
				</div>
			</div>
			<Paginator bind:settings={paginationSettings} on:change={updateTable} />
		{:else if tabSet === 2}
			<div class="space-y-8">
				{#if loaded && pipeline.statistics}
					<div class="grid gap-8 section-wrapper p-4 text-center">
						<div class="p-4 space-y-4 bordered-soft">
							<h3 class="h3">Status</h3>
							<div class="max-w-full" use:chart={statusPlotOptions} />
						</div>
						<div class="p-4 space-y-4 bordered-soft">
							<h3 class="h3">Errors</h3>
							<div class="max-w-full" use:chart={errorsPlotOptions} />
						</div>
						<div class="p-4 space-y-4 bordered-soft">
							<h3 class="h3">IO</h3>
							<div class="max-w-full" use:chart={ioPlotOptions} />
						</div>
						<div class="p-4 space-y-4 bordered-soft">
							<h3 class="h3">Usage per Month</h3>
							<div class="max-w-full" use:chart={usagePlotOptions} />
						</div>
					</div>
				{:else}
					<h3 class="section-wrapper h3 text-center py-8">Statistics are currently unavailable</h3>
				{/if}
			</div>
		{/if}
	</div>
</div>
