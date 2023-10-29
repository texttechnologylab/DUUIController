<script lang="ts">
	import { v4 as uuidv4 } from 'uuid'
	import { goto } from '$app/navigation'
	import PipelineComponent from '$lib/components/PipelineComponent.svelte'

	import {
		faArrowLeft,
		faClose,
		faDoorClosed,
		faDoorOpen,
		faHourglassEnd,
		faHourglassStart,
		faPlus,
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
	import type { TableSource, ToastSettings } from '@skeletonlabs/skeleton'
	import type { PageServerData } from './$types'
	import { datetimeToString, progresAsPercent } from '$lib/utils/text'
	import { getDuration } from '$lib/utils/time'
	import { progressMaximum } from '$lib/duui/process'
	import type { DUUIComponent } from '$lib/duui/component'
	import { isActive } from '$lib/duui/monitor'
	import { TabGroup, Tab } from '@skeletonlabs/skeleton'
	import { page } from '$app/stores'
	import { onMount } from 'svelte'

	export let data: PageServerData
	let { pipeline, processes } = data

	let status: string = 'Unknown'
	let progress: number = 0

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

	pipeline.components.forEach((component: DUUIComponent) => {
		if (!component.id) {
			component.id = uuidv4()
		}
	})

	async function deleteComponent(event: CustomEvent<any>): Promise<void> {
		pipeline.components = pipeline.components.filter(
			(component: DUUIComponent, index: number, array: DUUIComponent[]) => {
				if (component.id !== event.detail.id) return component
			}
		)

		let response = await fetch('/pipelines/api/update', {
			method: 'POST',
			headers: {
				'Content-Type': 'application/json'
			},
			body: JSON.stringify(pipeline)
		})

		if (response.ok) {
			const t: ToastSettings = {
				message: 'Component has been deleted',
				timeout: 4000,
				background: 'variant-filled-warning'
			}
			toastStore.trigger(t)
		}
		return
	}

	const modalStore = getModalStore()
	const toastStore = getToastStore()

	const updatePipeline = async () => {
		let response = await fetch('/pipelines/api/update', {
			method: 'POST',
			headers: {
				'Content-Type': 'application/json'
			},
			body: JSON.stringify(pipeline)
		})

		if (response.ok) {
			const t: ToastSettings = {
				message: 'Changes saved successfully',
				timeout: 4000,
				background: 'variant-filled-success'
			}
			toastStore.trigger(t)
		}
	}

	let startingService: boolean = false
	let stoppingService: boolean = false

	const startService = async () => {
		startingService = true
		let response = await fetch('/pipelines/api/service/start', {
			method: 'POST',
			headers: {
				'Content-Type': 'application/json'
			},
			body: JSON.stringify(pipeline)
		})

		if (response.ok) {
			const json = await response.json()
			pipeline.serviceStartTime = json.serviceStartTime
		}

		startingService = false
	}

	const stopService = async () => {
		stoppingService = true
		let response = await fetch('/pipelines/api/service/stop', {
			method: 'DELETE',
			headers: {
				'Content-Type': 'application/json'
			},
			body: JSON.stringify(pipeline)
		})

		if (response.ok) {
			pipeline.serviceStartTime = 0
		}

		stoppingService = false
	}

	const onMaybeDeletePipeline = async (e: SubmitEvent) => {
		let button = e.submitter as HTMLButtonElement

		if (!button) {
			return
		}

		if (button.id === 'update') {
			updatePipeline()
		} else if (button.id === 'service') {
			if (startingService || stoppingService) return

			if (pipeline.serviceStartTime === 0) {
				startService()
			} else {
				stopService()
			}
		} else {
			new Promise<boolean>((resolve) => {
				const modal: ModalSettings = {
					type: 'confirm',
					title: 'Please Confirm',
					body: `Are you sure you wish to delete ${pipeline.name}?`,
					response: (r: boolean) => {
						resolve(r)
					}
				}
				modalStore.trigger(modal)
			}).then(async (accepted: boolean) => {
				if (accepted) {
					const data = new FormData(e.target as HTMLFormElement)

					const response = await fetch((e.target as HTMLFormElement).action, {
						method: 'POST',
						body: data,
						headers: {
							'x-sveltekit-action': 'true'
						}
					})

					if (response.ok) {
						goto('/pipelines')
						const t: ToastSettings = {
							message: 'Pipeline successfully deleted.',
							timeout: 4000,
							background: 'variant-filled-success'
						}
						getToastStore().trigger(t)
					} else {
						const t: ToastSettings = {
							message: 'Pipeline could not be deleted.',
							timeout: 4000,
							background: 'variant-filled-warning'
						}
						getToastStore().trigger(t)
					}
				}
			})
		}
	}
</script>

<div class="container h-full flex-col mx-auto flex gap-4">
	<!-- HEADER -->
	<header class="grow self-stretch space-y-4">
		<h1 class="h2 font-bold grow text-center md:my-8">
			{pipeline.name}
		</h1>

		<div class="flex items-center justify-start">
			<button on:click={() => goto('/pipelines')} class="btn rounded-md hover:variant-soft-surface"
				><Fa icon={faArrowLeft} />
				<span class="hidden md:block">Pipelines</span>
			</button>
			<form method="POST" on:submit|preventDefault={onMaybeDeletePipeline}>
				<button id="service" class="btn rounded-md hover:variant-soft-surface">
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
			</form>

			<button
				class="btn rounded-md hover:variant-soft-surface ml-auto"
				on:click={() => goto('/process?pipeline=' + pipeline.id)}
			>
				<span><Fa icon={faPlus} /></span>
				<span>Create new process</span>
			</button>
		</div>

		<div
			class="grid grid-cols-3 justify-between items-center variant-soft-surface rounded-md shadow-lg"
		>
			<TabGroup active="variant-filled-surface rounded-md" border="none">
				<Tab
					bind:group={tabSet}
					name="settings"
					value={0}
					on:click={() => {
						$page.url.searchParams.set('tab', '0')
						goto($page.url)
					}}
				>
					<span>Settings</span>
				</Tab>
				<Tab
					bind:group={tabSet}
					name="processes"
					value={1}
					on:click={() => {
						$page.url.searchParams.set('tab', '1')
						goto($page.url)
					}}>Processes</Tab
				>
			</TabGroup>
		</div>
	</header>

	{#if tabSet === 0}
		<div class="grid md:grid-cols-2 gap-4">
			<form
				class="flex flex-col gap-4 variant-soft-surface rounded-md shadow-lg p-4"
				method="POST"
				on:submit|preventDefault={onMaybeDeletePipeline}
			>
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
						class="textarea resize-none border-2 input"
						rows="4"
						placeholder="Enter a description..."
						bind:value={pipeline.description}
					/>
				</label>

				<div class="flex justify-between">
					<button class="btn variant-filled-primary shadow-lg" type="submit" id="update">
						<span>Update</span>
					</button>

					<button class="btn variant-filled-error shadow-lg" type="submit" id="delete">
						<span>Delete</span>
					</button>
				</div>
			</form>
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
								active={isActive(status)}
								completed={progress >= pipeline.components.indexOf(component)}
								on:remove={deleteComponent}
								on:update={updatePipeline}
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
