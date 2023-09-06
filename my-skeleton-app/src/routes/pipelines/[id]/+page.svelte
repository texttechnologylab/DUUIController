<script lang="ts">
	import PipelineComponent from '$lib/components/PipelineComponent.svelte'
	import type { DUUIPipelineComponent } from '$lib/data'
	import { currentPipelineStore } from '$lib/store'
	import { faCancel, faCog, faRocket } from '@fortawesome/free-solid-svg-icons'
	import { type ConicStop, ConicGradient, ProgressRadial } from '@skeletonlabs/skeleton'
	import { onMount } from 'svelte'
	import { dndzone, type DndEvent } from 'svelte-dnd-action'
	import Fa from 'svelte-fa'
	import { flip } from 'svelte/animate'
	import { v4 as uuidv4 } from 'uuid'

	export let data

	let { pipeline } = data

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

		fetch('http://192.168.2.122:2605/pipeline', {
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
			const response = await fetch('http://192.168.2.122:2605/pipeline/status/' + pipeline.id, {
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

	async function runPipeline() {
		waiting = true
		cancelled = false
		const response = await fetch('http://192.168.2.122:2605/pipeline/start/' + pipeline.id, {
			method: 'POST',
			mode: 'cors'
		})

		result = await response.text()
		waiting = false
	}

	function cancelPipeline() {
		fetch('http://192.168.2.122:2605/pipeline/stop/' + pipeline.id, {
			method: 'POST',
			mode: 'cors',
		})

		pipeline.status = 'Cancelled'
		result = 'Cancelled'
		cancelled = true
		waiting = false
	}
</script>

<div class="container h-full flex-col mx-auto flex gap-4">
	<div class="grow self-stretch">
		<div class="flex items-center">
			<p class="h2 font-bold grow">{pipeline.name}</p>
			<button class="btn-icon variant-glass-primary md:hidden"><Fa size="lg" icon={faCog} /></button
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
	<div class="grid md:grid-cols-2">
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
		<p class="hidden md:block">Placeholder</p>
	</div>

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
			<button class="btn variant-filled-primary" on:click={runPipeline}>
				<span><Fa icon={faRocket} /></span>
				<span>Start Pipeline</span>
			</button>
		{/if}

		{#if !waiting && result !== '' && !cancelled}
			<button class="btn variant-ringed-primary" on:click={() => console.log(result)}
				>Get result</button
			>
		{/if}
	</div>
</div>
