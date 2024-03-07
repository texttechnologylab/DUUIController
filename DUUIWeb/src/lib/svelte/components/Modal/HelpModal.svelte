<!--
	@component
	A modal component that displays helpful information about the web interface.
-->
<script lang="ts">
	import { DUUIDrivers, type DUUIComponent } from '$lib/duui/component'
	import { faClone, faClose, faEdit, faRocket } from '@fortawesome/free-solid-svg-icons'
	import { getModalStore } from '@skeletonlabs/skeleton'
	import Fa from 'svelte-fa'
	import DriverIcon from '../DriverIcon.svelte'
	import { dndzone } from 'svelte-dnd-action'
	import { flip } from 'svelte/animate'
	import PipelineComponent from '../PipelineComponent.svelte'

	const modalStore = getModalStore()

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

<div
	class="z-50 bg-surface-50-900-token w-full max-w-screen-lg rounded-md overflow-hidden border border-color"
>
	<div class="modal-header bg-surface-100-800-token">
		<h3 class="h3">Getting Started</h3>
		<button on:click={modalStore.close}>
			<Fa icon={faClose} size="lg" />
		</button>
	</div>
	<div class="modal-body md:p-8 space-y-12">
		<div class="p-8 space-y-12 text-lg grid justify-center">
			<div class="space-y-4">
				<p class="font-bold h2">About Docker Unified UIMA Interface (DUUI)</p>
				<p>
					DUUI automates big data analysis using containers called
					<a class="anchor" href="/documentation#pipeline">Pipelines</a> for
					<a href="/documentation#component" class="anchor">Components</a> that make up an
					executable workflow. Each Component must follow the implementation as defined by DUUI and
					is therefore a <a href="https://uima.apache.org/" class="anchor">UIMA</a> conform annotator.
					A pipeline consists of one or multiple Components that can be customized through various settings.
				</p>
			</div>

			<hr class="hr" />
			<div class="grid gap-4">
				<p class="font-bold h2">Component</p>
				<p>
					A Component is instantiated and controlled by a Driver indicated by an icon. The Driver
					also defines settings that influence a Component's behavior. These settings can be changed
					by clicking the <Fa class="inline" icon={faEdit} /> icon.
				</p>
				<div class="flex flex-wrap justify-start gap-4">
					{#each DUUIDrivers as driver}
						<div class="grid grid-cols-[40px_1fr] items-center gap-4">
							<DriverIcon {driver} />
							<p>{driver}</p>
						</div>
					{/each}
				</div>
			</div>
			<hr class="hr" />
			<div class="grid gap-4">
				<p class="font-bold h2">Pipeline</p>
				<p>The order of execution can be changed via Drag & Drop.</p>
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
			<p>
				Pipelines can be created from scratch or using a Template in the <a
					href="/pipelines/build"
					class="anchor">Pipeline Builder</a
				>.
			</p>
			<hr class="hr" />
			<div class="grid gap-4">
				<p class="font-bold h2">Process</p>
				<p>
					Once you have a created a Pipeline, you can start using it to analyze files or plain text.
					Using an external cloud storage as the input or output requires you to connect your
					provider of choice with DUUI on the <a href="/account" class="anchor">Account</a> page.
				</p>
				<p>
					Processes can be created by clicking the
					<span class="badge variant-soft-primary">
						<Fa icon={faRocket} />
						<span>Process</span>
					</span>
					button on a pipeline page reached from the
					<a href="/pipelines" class="anchor">Dashboard</a>. You can explore the results of past
					processes or monitor active ones from the
					<span class="badge variant-soft-primary">Processes</span> tab.
				</p>
			</div>
		</div>
		<hr class="hr" />
		<div class="p-8 flex justify-center gap-4">
			<a href="/documentation" class="anchor">Further reading</a>
			<a href="/documentation/api" class="anchor">Use the API instead</a>
		</div>
	</div>
</div>
