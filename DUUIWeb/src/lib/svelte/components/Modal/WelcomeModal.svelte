<!--
	@component
	A modal component that displays a welcome tour.
-->
<script lang="ts">
	import {
		faCheck,
		faChevronLeft,
		faChevronRight,
		faClose,
		faEdit,
		faMagnifyingGlass,
		faMapSigns
	} from '@fortawesome/free-solid-svg-icons'
	import { ProgressBar, getModalStore } from '@skeletonlabs/skeleton'
	import Fa from 'svelte-fa'

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
</script>

<div class="z-50 bg-surface-50-900-token w-modal rounded-md overflow-hidden border-2 border-color">
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
		<div class="space-y-4 p-8 text-lg">
			{#if step === 0}
				<div class="grid space-y-12">
					<div class="space-y-4">
						<p>
							This web interface complements the Java framework <a
								href="https://github.com/texttechnologylab/DockerUnifiedUIMAInterface"
								class="anchor">Docker Unified UIMA Interface</a
							> and is designed to help you automate the analysis of large quantities of natural language
							for experienced and inexperienced users.
						</p>
						<p class="p-4 bordered-soft rounded-md variant-soft-primary">
							There is no need for programming knowledge to get started.
						</p>
					</div>
					<hr class="hr" />
					<p>Do you want to start a small tour through the web interface to get started?</p>
					<div class="grid md:grid-cols-2 items-center gap-4">
						<button class="button-primary button-modal" on:click={startTour}>
							<Fa icon={faMapSigns} />
							<span>Start</span>
						</button>
						<button class="button-neutral button-modal" on:click={finish}>
							<span>Explore on your own</span>
							<Fa icon={faMagnifyingGlass} />
						</button>
					</div>
				</div>
			{:else if step === 1}
				<div class="space-y-8">
					<h2 class="h4">Pipelines</h2>
					<p>
						A pipeline is a collection of components or Analysis Engines that can be executed.
						During an analysis process, the components in the pipeline are executed one after
						another.
					</p>
					<p class="text-center p-4 bordered-soft rounded-md variant-soft-primary">
						You can create pipelines in the <a href="/pipelines/builder" class="anchor">Builder</a> and
						manage them from the Dashboard. Once created a pipeline can be used as a template in the
						builder.
					</p>
				</div>
			{:else if step === 2}
				<div class="space-y-8">
					<h2 class="h4">Components</h2>
					<p>
						Components are the part of DUUI that actually do the processing and therefore offer the
						most settings. When creating a pipeline you can choose from a set of predefined
						components or create your own. Once added to the pipeline, a component can be edited by
						clicking the <Fa icon={faEdit} class="inline" /> icon.
					</p>

					<p class="text-center p-4 bordered-soft rounded-md variant-soft-primary">
						You can create pipelines in the <a href="/pipelines/builder" class="anchor">Builder</a> and
						manage them from the Dashboard.
					</p>
				</div>
			{:else if step === 3}
				<div class="space-y-8">
					<h2 class="h4">Processes</h2>
					<p>
						A process manages the flow of data and pipeline execution. Starting a process is
						possible on a pipeline page. On the process creation screen you are asked to select an
						input, output and optionally settings that influence the process behavior.
					</p>
					<p class="p-4 bordered-soft rounded-md variant-soft-primary">
						DUUI provides a way to use different Cloud storages as input and output locations. To
						use them you must first establish a connection on your <a
							href="/pipelines/builder"
							class="anchor">Account</a
						> page
					</p>
				</div>
			{:else if step === 4}
				<div class="space-y-8">
					<h2 class="h4">Documents</h2>
					<p>
						The document is the smallest unit in DUUI and holds the data to be processed.
						Additionally documents track metrics about performance, integrity and performed
						annotations.
					</p>
					<p class="p-4 bordered-soft rounded-md variant-soft-primary">
						Completed documents can be downloaded directly from the output location when opening the
						drawer (clicking on a table row) from the process page.
					</p>
				</div>
			{/if}
		</div>
		{#if step > 0}
			<div class="p-8 border-t border-color flex justify-between items-center gap-8">
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
