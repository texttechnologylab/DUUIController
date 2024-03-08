<!--
	@component
	A modal component that displays helpful information about the web interface.
-->
<script lang="ts">
	import { DUUIDrivers, type DUUIComponent } from '$lib/duui/component'
	import {
		faCheck,
		faChevronLeft,
		faChevronRight,
		faClose,
		faEdit,
		faMagnifyingGlass,
		faMapSigns,
		faRocket
	} from '@fortawesome/free-solid-svg-icons'
	import { ProgressBar, getModalStore } from '@skeletonlabs/skeleton'
	import { dndzone } from 'svelte-dnd-action'
	import Fa from 'svelte-fa'
	import { flip } from 'svelte/animate'
	import DriverIcon from '../DriverIcon.svelte'
	import PipelineComponent from '../PipelineComponent.svelte'
	import Tip from '../Tip.svelte'

	const modalStore = getModalStore()
	let step: number = 0

	const startTour = async () => {
		step = 1
	}

	let max: number = 4

	const next = () => {
		step += 1
	}

	const back = () => {
		step -= step === 0 ? 0 : 1
	}

	const finish = async () => {
		await fetch('/api/users', {
			method: 'PUT',
			body: JSON.stringify({ 'preferences.tutorial': false })
		})
		modalStore.close()
	}

	const handleDndConsider = (event: CustomEvent<DndEvent<DUUIComponent>>) => {
		exampleComponents = [...event.detail.items]
	}

	const handleDndFinalize = (event: CustomEvent<DndEvent<DUUIComponent>>) => {
		exampleComponents = [...event.detail.items]
		exampleComponents.forEach(
			(c: { index: any; oid: any }) =>
				(c.index = exampleComponents.map((c: DUUIComponent) => c.oid).indexOf(c.oid))
		)
	}

	let exampleComponents: DUUIComponent[] = [
		{
			name: 'Component 1',
			driver: 'DUUIDockerDriver',
			target: 'temp',
			oid: '',
			id: '1',
			tags: [],
			description: '',
			options: {
				use_GPU: true,
				docker_image_fetching: true,
				scale: 1,
				keep_alive: false,
				registry_auth: {
					username: '',
					password: ''
				},
				constraints: [],
				labels: [],
				host: '',
				ignore_200_error: true
			},
			parameters: {},
			pipeline_id: '',
			user_id: null,
			index: 0
		},
		{
			name: 'Component 2',
			driver: 'DUUIRemoteDriver',
			target: 'temp',
			oid: '',
			id: '2',
			tags: [],
			description: '',
			options: {
				use_GPU: true,
				docker_image_fetching: true,
				scale: 1,
				keep_alive: false,
				registry_auth: {
					username: '',
					password: ''
				},
				constraints: [],
				labels: [],
				host: '',
				ignore_200_error: true
			},
			parameters: {},
			pipeline_id: '',
			user_id: null,
			index: 1
		}
	]
</script>

<div class="z-50 bg-surface-50-900-token w-modal rounded-md border-2 border-color max-h-1/2">
	<div class="bg-surface-100-800-token">
		<div class="modal-header">
			<h1 class="h3">Welcome to DUUI Web!</h1>
			<button on:click={finish} class="transition-300 hover:text-error-500">
				<Fa icon={faClose} size="lg" />
			</button>
		</div>
		{#if step > 0}
			<div class="p-4 border-b border-color">
				<ProgressBar
					{max}
					bind:value={step}
					height="h-4"
					rounded="rounded-full"
					meter="bg-primary-500"
					class="section-wrapper"
				/>
			</div>
		{/if}
	</div>
	<div class="modal-body space-y-4">
		<div class="space-y-4 p-8 leading-relaxed">
			{#if step === 0}
				<div class="grid space-y-6">
					<div class="space-y-4">
						<p class="font-bold h2">About DUUI</p>
						<p>
							DUUI automates big data analysis using containers called
							<span class="badge mx-2 px-2 variant-soft-primary">Pipelines</span> for
							<span class="badge mx-2 px-2 variant-soft-primary">Components</span> that make up an
							executable workflow. Each Component must follow the implementation as defined by DUUI
							and is therefore a
							<a href="https://uima.apache.org/" class="anchor" target="_blank">UIMA</a> conform annotator.
							A pipeline consists of one or multiple Components that can be customized through various
							settings.
						</p>
					</div>
					<Tip
						>You can always open this guide from the navigation at the top under Documentation &gt
						Help</Tip
					>
					<hr class="hr" />
					<p>Do you want to start a small tour explaining the parts that make up DUUI?</p>
					<div class="grid md:grid-cols-2 items-center gap-4">
						<button class="button-primary button-modal" on:click={startTour}>
							<Fa icon={faMapSigns} />
							<span>Start Tour</span>
						</button>
						<button class="button-neutral button-modal" on:click={finish}>
							<span>Explore on your own</span>
							<Fa icon={faMagnifyingGlass} />
						</button>
					</div>
				</div>
			{:else if step === 1}
				<div class="space-y-8">
					<p class="font-bold h2">Pipeline</p>
					<p>
						A pipeline is a collection of components or Analysis Engines that can be executed.
						During an analysis process, the components in the pipeline are executed one after
						another. You can create pipelines in the <span
							class="badge mx-2 px-2 variant-soft-primary">Builder</span
						> and manage them from the Dashboard.
					</p>
				</div>
				<Tip>Once created a pipeline can be used as a template in the builder.</Tip>
			{:else if step === 2}
				<div class="space-y-8">
					<h2 class="h4">Components</h2>
					<p>
						A Component is instantiated and controlled by a Driver indicated by an icon. The Driver
						also defines settings that influence a Component's behavior. These settings can be
						changed by clicking the
						<span class="badge !mx-2 px-4 variant-soft-primary">
							<Fa icon={faEdit} />
							<span class="pt-1">Edit</span>
						</span>
						button.
					</p>
					<div class="flex flex-wrap justify-center items-center gap-4">
						{#each DUUIDrivers as driver}
							<div class="flex flex-col items-center gap-4">
								<DriverIcon {driver} />
								<p>{driver.replace('DUUI', '').replace('Driver', '')}</p>
							</div>
						{/each}
					</div>
					<Tip>The order of execution can be changed via Drag & Drop.</Tip>
					<ul
						use:dndzone={{ items: exampleComponents, dropTargetStyle: {} }}
						on:consider={(event) => handleDndConsider(event)}
						on:finalize={(event) => handleDndFinalize(event)}
						class="grid gap-4 !cursor-move"
					>
						{#each exampleComponents as component (component.id)}
							<div animate:flip={{ duration: 300 }} class="relative !cursor-move">
								<PipelineComponent editable={false} {component} />
							</div>
						{/each}
					</ul>
				</div>
			{:else if step === 3}
				<div class="space-y-8">
					<h2 class="h4">Processes</h2>
					<p>
						Once you have a created a Pipeline, you can start using it to analyze files or plain
						text by starting it in a process. A process manages the flow of data and pipeline
						execution.
					</p>
					<p>
						Processes can be created by clicking the
						<span class="badge !mx-2 px-4 variant-soft-primary">
							<Fa icon={faRocket} />
							<span>Process</span>
						</span>
						button on a pipeline page reached from the
						<span class="badge variant-soft-primary px-2 mx-2">Dashboard</span>. You can explore the
						results of past processes or monitor active ones from the
						<span class="badge variant-soft-primary px-2 mx-2">Processes</span> tab.
					</p>
					<Tip>
						Using an external cloud storage as the input or output requires you to connect your
						provider of choice with DUUI on the Account page under connections.
					</Tip>
				</div>
			{:else if step === 4}
				<div class="space-y-8">
					<h2 class="h4">Documents</h2>
					<p>
						Documents are the smallest unit in DUUI holding the data to be processed. Additionally
						documents track metrics about performance, integrity and performed annotations.
					</p>
					<Tip>
						Completed documents can be downloaded directly from the output location when opening the
						drawer (clicking on a table row) from the process page.
					</Tip>
				</div>
			{/if}
		</div>
		{#if step > 0}
			<div class="p-8 py-4 border-t border-color flex justify-between items-center gap-8">
				<button class="button-neutral !justify-center w-[160px]" on:click={back} type="button">
					<Fa icon={faChevronLeft} />
					<span>Back</span>
				</button>

				{#if step === max}
					<button class="button-neutral !justify-center w-[160px]" on:click={finish}>
						<span>Start Working</span>
						<Fa icon={faCheck} />
					</button>
				{:else}
					<button type="button" class="button-neutral !justify-center w-[160px]" on:click={next}>
						<span>Next</span>
						<Fa icon={faChevronRight} />
					</button>
				{/if}
			</div>
		{/if}
	</div>
</div>
