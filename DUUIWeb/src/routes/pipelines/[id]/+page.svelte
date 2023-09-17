<script lang="ts">
	import { goto } from '$app/navigation'
	import PipelineComponent from '$lib/components/PipelineComponent.svelte'
	import {
		blankComponent,
		DUUIStatus,
		type DUUIPipelineComponent,
		type DUUIProcess
	} from '$lib/data'
	import { faAdd, faArrowLeft, faCancel, faCog, faRocket } from '@fortawesome/free-solid-svg-icons'
	import { ProgressRadial, type ModalSettings, getModalStore } from '@skeletonlabs/skeleton'
	import { onMount } from 'svelte'
	import { dndzone, type DndEvent } from 'svelte-dnd-action'
	import Fa from 'svelte-fa'
	import { flip } from 'svelte/animate'

	import { getToastStore } from '@skeletonlabs/skeleton'
	import type { ToastSettings } from '@skeletonlabs/skeleton'
	import type { ActionData, PageServerData } from './$types'

	export let data: PageServerData
	let { pipeline, processes } = data

	let building: boolean = false
	let exception: string
	let result: string = ''
	let annotations: Map<string, number> = new Map()
	let documentText: string = 'This is a test sentence. My name is Cedric Borkowski.'

	let status: string = DUUIStatus.Unknown
	let progress: number = 0
	let process_id: string

	export let form: ActionData

	// let runningProcesses: number = processes.filter((process) => process.status === DUUIStatus.Running).length;

	let flipDurationMs = 300

	function handleDndConsider(event: CustomEvent<DndEvent<DUUIPipelineComponent>>) {
		pipeline.components = event.detail.items
		pipeline.components = [...pipeline.components]
	}

	function handleDndFinalize(event: CustomEvent<DndEvent<DUUIPipelineComponent>>) {
		pipeline.components = event.detail.items
		pipeline.components = [...pipeline.components]
	}

	onMount(() => {
		async function checkStatus() {
			if (process_id === undefined || status === DUUIStatus.Completed) {
				return
			}

			const response = await fetch('http://127.0.0.1:2605/processes/' + process_id, {
				method: 'GET',
				mode: 'cors'
			})

			const process = await response.json()
			status = process.status
			progress = process.progress
			building = process.status === DUUIStatus.Setup

			if (process.status === DUUIStatus.Completed || process.status === DUUIStatus.Cancelled) {
				clearInterval(interval)
			}
		}

		const interval = setInterval(checkStatus, 2000)
		checkStatus()

		return () => clearInterval(interval)
	})

	pipeline.components.forEach((component: DUUIPipelineComponent) => {
		component.id = pipeline.components.indexOf(component)
	})

	function deleteComponent(event: CustomEvent<any>): void {
		pipeline.components = pipeline.components.filter(
			(component: DUUIPipelineComponent, index: number, array: DUUIPipelineComponent[]) => {
				if (component.id !== event.detail.id) return component
			}
		)

		fetch('http://127.0.0.1:2605/pipelines', {
			method: 'PUT',
			mode: 'cors',
			body: JSON.stringify(pipeline)
		})
	}

	async function updatePipeline() {
		const response = await fetch('http://127.0.0.1:2605/pipelines', {
			method: 'PUT',
			mode: 'cors',
			body: JSON.stringify(pipeline)
		})

		const message = await response.json()
		if (message.message === 'success') {
			const t: ToastSettings = {
				message: 'Changes saved successfully',
				timeout: 4000,
				background: 'variant-filled-surface'
			}
			getToastStore().trigger(t)
		}
	}

	async function runPipeline() {
		building = true

		const jresult = await fetch('http://127.0.0.1:2605/processes', {
			method: 'POST',
			mode: 'cors',
			body: JSON.stringify({
				pipeline_id: pipeline.id,
				options: {
					document: documentText
				}
			})
		})

		const j = await jresult.json()
		if (jresult.status === 400) {
			exception = j.error
			const t: ToastSettings = {
				message: exception,
				timeout: 4000,
				background: 'variant-filled-error'
			}
			getToastStore().trigger(t)
		} else {
			process_id = j.id
		}
	}

	function cancelPipeline() {
		if (status === DUUIStatus.Completed) {
			const t: ToastSettings = {
				message: 'Pipeline has already completed',
				timeout: 4000,
				background: 'variant-filled-warning'
			}
			getToastStore().trigger(t)
			return
		}

		if (status === DUUIStatus.Cancelled) {
			const t: ToastSettings = {
				message: 'Pipeline has already cancelled',
				timeout: 4000,
				background: 'variant-filled-warning'
			}
			getToastStore().trigger(t)
			return
		}

		fetch('http://127.0.0.1:2605/processes/' + process_id, {
			method: 'PUT',
			mode: 'cors'
		})

		const t: ToastSettings = {
			message: 'Pipeline has been cancelled',
			timeout: 4000,
			background: 'variant-filled-warning'
		}
		getToastStore().trigger(t)
	}

	function addComponent() {
		pipeline.components = [...pipeline.components, blankComponent(pipeline.components.length + 1)]
	}

	const modalStore = getModalStore()

	const onMaybeDeletePipeline = (e: SubmitEvent) => {
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

	const getResult = async () => {
		const temp = await fetch('http://127.0.0.1:2605/pipelines/' + pipeline.id + '/result', {
			method: 'GET',
			mode: 'cors'
		})
		const r = await temp.json()
		if (r) {
			result = r
		}
		console.log(result)
	}
</script>

<div class="container h-full flex-col mx-auto flex gap-4">
	<!-- HEADER -->
	<header class="grow self-stretch">
		<div class="flex items-center space-x-2 my-8">
			<button on:click={() => goto('/pipelines')} class="btn-icon shadow-lg variant-glass-primary"
				><Fa size="lg" icon={faArrowLeft} /></button
			>
			<h1 class="h2 font-bold grow text-center">
				{pipeline.name} - Status: {status.toUpperCase()}
			</h1>
			<button class="btn-icon shadow-lg variant-glass-primary lg:hidden">
				<Fa size="lg" icon={faCog} />
			</button>
		</div>
	</header>

	<!-- Settings & Recent processes -->

	<div class="grid lg:grid-cols-2 lg:gap-8">
		<!-- Settings -->
		<div class="space-y-4">
			<h2 class="h3 hidden lg:block px-4">Settings</h2>
			<div class="card space-y-4 p-4">
				<form
					class=" flex gap-4 items-end"
					method="POST"
					on:submit|preventDefault={onMaybeDeletePipeline}
				>
					<label class="label grow">
						<span>Name</span>
						<input
							bind:value={pipeline.name}
							class="input focus-within:outline-primary-400"
							type="text"
						/>
					</label>
					<button class="btn variant-filled-error rounded-sm shadow-lg" type="submit">
						<span>Delete</span>
					</button>
				</form>
				<div>
					{#if building || status === DUUIStatus.Running}
						<div class="flex items-center gap-4">
							<ProgressRadial stroke={120} width="w-16" meter="stroke-primary-400" />
							<button class="btn variant-ghost-error" on:click={cancelPipeline}>
								<span><Fa icon={faCancel} /></span>
								<span>Cancel Pipeline</span>
							</button>
						</div>
					{:else}
						<div class="flex gap-8 justify-center items-center">
							<button class="btn variant-filled-primary" on:click={runPipeline}>
								<span><Fa icon={faRocket} /></span>
								<span>Start Pipeline</span>
							</button>
							<!-- {#if !building && !cancelled}
								<button class="btn variant-ringed-primary" on:click={getResult}>Get result</button>
							{/if} -->
						</div>
					{/if}
				</div>
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
			<p class="h3 hidden lg:block">Components</p>
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
							on:deletion={deleteComponent}
							on:remove={deleteComponent}
						/>
					</div>
				{/each}
			</ul>
			<div class="flex justify-center">
				<button class="btn-icon variant-filled-primary" on:click={addComponent}>
					<Fa icon={faAdd} />
				</button>
			</div>
		</div>
	</div>
</div>
