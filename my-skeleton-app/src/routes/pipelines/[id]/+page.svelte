<script lang="ts">
	import { goto } from '$app/navigation'
	import PipelineComponent from '$lib/components/PipelineComponent.svelte'
	import Card from '$lib/components/containers/Card.svelte'
	import { blankComponent, type DUUIPipelineComponent } from '$lib/data'
	import {
		faAdd,
		faArrowLeft,
		faCancel,
		faCog,
		faRemove,
		faRocket
	} from '@fortawesome/free-solid-svg-icons'
	import { ProgressRadial, type ModalSettings, getModalStore } from '@skeletonlabs/skeleton'
	import { onMount } from 'svelte'
	import { dndzone, type DndEvent } from 'svelte-dnd-action'
	import Fa from 'svelte-fa'
	import { flip } from 'svelte/animate'

	export let data

	let { pipeline } = data

	import { Toast, getToastStore } from '@skeletonlabs/skeleton'
	import type { ToastSettings, ToastStore } from '@skeletonlabs/skeleton'

	function handleDndConsider(event: CustomEvent<DndEvent<DUUIPipelineComponent>>) {
		pipeline.components = event.detail.items
		pipeline.components = [...pipeline.components]
	}

	function handleDndFinalize(event: CustomEvent<DndEvent<DUUIPipelineComponent>>) {
		pipeline.components = event.detail.items
		pipeline.components = [...pipeline.components]
	}

	let flipDurationMs = 300

	pipeline.components.forEach((component: DUUIPipelineComponent) => {
		component.id = pipeline.components.indexOf(component)
	})

	function deleteComponent(event: CustomEvent<any>): void {
		pipeline.components = pipeline.components.filter(
			(component: DUUIPipelineComponent, index: number, array: DUUIPipelineComponent[]) => {
				if (component.id !== event.detail.id) return component
			}
		)

		fetch('http://127.0.0.1:2605/pipeline', {
			method: 'PUT',
			mode: 'cors',
			body: JSON.stringify(pipeline)
		})
	}

	let waiting: boolean = pipeline.status === 'Running'
	let cancelled: boolean = pipeline.status === 'Cancelled'
	let result: string = ''

	onMount(() => {
		async function checkStatus() {
			const response = await fetch('http://127.0.0.1:2605/pipeline/status/' + pipeline.id, {
				method: 'GET',
				mode: 'cors'
			})

			const temp = await response.json()
			waiting = temp.status === 'Running'
			cancelled = temp.status === 'Cancelled'
			if (!waiting) {
				clearInterval(interval)
			}
		}

		const interval = setInterval(checkStatus, 2000)
		checkStatus()

		return () => clearInterval(interval)
	})

	async function updatePipeline() {
		const response = await fetch('http://127.0.0.1:2605/pipeline', {
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
		waiting = true
		cancelled = false

		await fetch('http://127.0.0.1:2605/pipeline', {
			method: 'PUT',
			mode: 'cors',
			body: JSON.stringify(pipeline)
		})

		pipeline.status = 'Running'

		const response = await fetch('http://127.0.0.1:2605/pipeline/start/' + pipeline.id, {
			method: 'POST',
			mode: 'cors'
		})

		result = await response.text()
		waiting = false
	}

	function cancelPipeline() {
		fetch('http://127.0.0.1:2605/pipeline/stop/' + pipeline.id, {
			method: 'POST',
			mode: 'cors'
		})

		pipeline.status = 'Cancelled'
		result = 'Cancelled'
		cancelled = true
		waiting = false

		const t: ToastSettings = {
			message: 'Pipeline has been cancelled',
			timeout: 4000,
			background: 'variant-filled-surface'
		}
		getToastStore().trigger(t)
	}

	function addComponent() {
		pipeline.components = [...pipeline.components, blankComponent(pipeline.id)]
	}

	const modalStore = getModalStore()

	const onMaybeDeletePipeline = () => {
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
		}).then(async (r: any) => {
			if (r) {
				const response = await fetch('http://192.168.2.122:2605/pipeline/' + pipeline.id, {
					method: 'DELETE',
					mode: 'cors'
				})

				const success = await response.json()
				goto('/pipelines')
			}
		})
	}
</script>

<div class="container h-full flex-col mx-auto flex gap-4">
	<button on:click={() => goto("/pipelines")} class="btn-icon shadow-lg variant-glass-primary"
		><Fa size="lg" icon={faArrowLeft} /></button
	>
	<div class="grow self-stretch">
		<div class="flex items-center space-x-2">
			<p class="h2 font-bold grow">{pipeline.name}</p>
			<button class="btn-icon shadow-lg variant-glass-primary lg:hidden"
				><Fa size="lg" icon={faCog} /></button
			>
		</div>

		{#if pipeline.status === 'Error'}
			<p class="text-error-400">{pipeline.status}</p>
		{:else if pipeline.status === 'Cancelled'}
			<p class="text-warning-400">{pipeline.status}</p>
		{:else if pipeline.status === 'Completed'}
			<p class="text-success-400">{pipeline.status}</p>
		{:else}
			<p>{pipeline.status}</p>
		{/if}
	</div>
	<div class="grid lg:grid-cols-2 lg:gap-8">
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
							on:deletion={deleteComponent}
							on:remove={deleteComponent}
						/>
					</div>
				{/each}
			</ul>
			<div class="flex items-center justify-center w-full gap-4 mt-8">
				{#if waiting}
					<div class="flex items-center flex-col gap-4">
						<ProgressRadial stroke={120} width="w-16" meter="stroke-primary-400" />
						<button class="btn variant-ghost-error" on:click={cancelPipeline}>
							<span><Fa icon={faCancel} /></span>
							<span>Cancel Pipeline</span>
						</button>
					</div>
				{:else}
					<div class="flex flex-col gap-8 justify-center items-center">
						<button class="btn-icon variant-filled-primary" on:click={addComponent}>
							<Fa icon={faAdd} />
						</button>
						<button class="btn variant-filled-primary" on:click={runPipeline}>
							<span><Fa icon={faRocket} /></span>
							<span>Start Pipeline</span>
						</button>
						{#if !waiting && result !== '' && !cancelled}
							<button class="btn variant-ringed-primary" on:click={() => console.log(result)}
								>Get result</button
							>
						{/if}
					</div>
				{/if}
			</div>
		</div>
		<div class="space-y-4 lg:flex-col hidden lg:flex">
			<p class="h3 hidden lg:block">Settings</p>
			<form action="" class="card shadow-lg flex flex-col gap-4 p-4">
				<label class="label">
					<span>Name</span>
					<input
						bind:value={pipeline.name}
						class="input focus-within:outline-primary-400"
						type="text"
					/>
				</label>
				<div class="grid self-end grid-cols-2 gap-5">
					<button
						class="btn variant-filled-success rounded-sm shadow-lg self-end"
						on:click={updatePipeline}
					>
						<span>Save Changes</span>
					</button>
					<button
						class="btn variant-filled-error rounded-sm shadow-lg self-end"
						on:click={onMaybeDeletePipeline}
					>
						<span>Delete</span>
					</button>
				</div>
			</form>
		</div>
	</div>
</div>
