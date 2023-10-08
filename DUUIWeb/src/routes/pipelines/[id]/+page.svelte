<script lang="ts">
	import { goto } from '$app/navigation'
	import PipelineComponent from '$lib/components/PipelineComponent.svelte'
	import { blankComponent, DUUIStatus, type DUUIPipelineComponent } from '$lib/data'
	import { faArrowLeft, faRocket } from '@fortawesome/free-solid-svg-icons'
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
	import type { ActionData, PageServerData } from './$types'
	import { getProgressPercent, getTimeDifference, toDateTimeString, toTitleCase } from '$lib/utils'

	export let data: PageServerData
	let { pipeline, processes } = data

	let status: string = DUUIStatus.Unknown
	let progress: number = 0

	let tableSource = processes.map((process, index, _) => {
		return {
			positon: index,
			status: toTitleCase(process.status),
			progress: getProgressPercent(process, pipeline) + ' %',
			startedAt: process.startedAt ? toDateTimeString(new Date(process.startedAt)) : '',
			duration: getTimeDifference(process.startedAt, process.finishedAt),
			input: toTitleCase(process.input.source),
			output: toTitleCase(process.output.type),
			process: process
		}
	})

	let tableData: TableSource = {
		head: ['Status', 'Start Time', 'Duration', 'Data source', 'Data output', 'Progress'],
		body: tableMapperValues(tableSource, [
			'status',
			'startedAt',
			'duration',
			'input',
			'output',
			'progress'
		]),
		meta: tableMapperValues(tableSource, ['process'])
	}

	export let form: ActionData

	let flipDurationMs = 300

	function handleDndConsider(event: CustomEvent<DndEvent<DUUIPipelineComponent>>) {
		pipeline.components = event.detail.items
		pipeline.components = [...pipeline.components]
	}

	function handleDndFinalize(event: CustomEvent<DndEvent<DUUIPipelineComponent>>) {
		pipeline.components = event.detail.items
		pipeline.components = [...pipeline.components]
	}

	pipeline.components.forEach((component: DUUIPipelineComponent) => {
		component.id = pipeline.components.indexOf(component)
	})

	async function deleteComponent(event: CustomEvent<any>): Promise<void> {
		pipeline.components = pipeline.components.filter(
			(component: DUUIPipelineComponent, index: number, array: DUUIPipelineComponent[]) => {
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

	const onMaybeDeletePipeline = async (e: SubmitEvent) => {
		let button = e.submitter as HTMLButtonElement

		if (!button) {
			return
		}

		if (button.id === 'update') {
			updatePipeline()
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
	<header class="grow self-stretch">
		<div class="flex items-center space-x-2">
			<button on:click={() => goto('/pipelines')} class="btn-icon shadow-lg variant-glass-primary"
				><Fa size="lg" icon={faArrowLeft} /></button
			>
			<h1 class="h2 font-bold grow text-center">
				{pipeline.name}
			</h1>
			<button
				class="btn-icon shadow-lg variant-glass-primary"
				on:click={() => goto('/process?pipeline=' + pipeline.id)}
			>
				<span><Fa icon={faRocket} /></span>
			</button>
		</div>
	</header>

	<!-- Settings & Recent processes -->

	<div class="grid lg:grid-cols-2 lg:gap-4">
		<!-- Settings -->
		<div class="space-y-4">
			<h2 class="h3 hidden lg:block px-4">Settings</h2>
			<div class="card space-y-4 p-4 rounded-md">
				<form
					class="flex flex-col gap-4"
					method="POST"
					on:submit|preventDefault={onMaybeDeletePipeline}
				>
					<label class="label grow col-span-2">
						<span>Name</span>
						<input
							bind:value={pipeline.name}
							class="border-2 input focus-within:outline-primary-400"
							type="text"
						/>
					</label>

					<label class="label grow col-span-2">
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
						<button class="btn variant-filled-error shadow-lg ml-auto" type="submit" id="delete">
							<span>Delete</span>
						</button>
					</div>
				</form>
			</div>

			<div class="space-y-4">
				<h2 class="h3 hidden lg:block px-4">Components</h2>
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
								active={status === DUUIStatus.Running}
								completed={progress >= component.id + 1}
								on:remove={deleteComponent}
								on:update={updatePipeline}
							/>
						</div>
					{/each}
				</ul>
			</div>

			<!-- {#if annotations.size > 0}
				<div class="card space-y-4 p-4">
					<p class="h4 hidden lg:block">Annotations</p>
					{#each annotations.entries() as [key, value]}
						<p>{key.split('.').at(-1)}: {value}</p>
					{/each}
				</div>
			{/if} -->
		</div>

		<div class="space-y-4">
			<h2 class="h3 hidden md:block px-4">Recent Processes</h2>
			<Table
				class="hidden md:block rounded-md shadow-lg "
				source={tableData}
				interactive
				on:selected={(e) => goto('/process/' + e.detail[0].id)}
			/>
			<button
				class="btn variant-filled-primary ml-auto rounded-md shadow-lg"
				on:click={() => goto('/process?pipeline=' + pipeline.id)}
			>
				<span><Fa icon={faRocket} /></span>
				<span>Create new process</span>
			</button>
		</div>
	</div>
</div>
