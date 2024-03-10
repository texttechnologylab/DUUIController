<script lang="ts">
	import { blankComponent } from '$lib/duui/component'
	import { exampleComponent, examplePipelineStore } from '$lib/store.js'
	import Chips from '$lib/svelte/components/Input/Chips.svelte'
	import TextInput from '$lib/svelte/components/Input/TextInput.svelte'
	import PipelineCard from '$lib/svelte/components/PipelineCard.svelte'
	import PipelineComponent from '$lib/svelte/components/PipelineComponent.svelte'
	import { faCheck, faClone, faEdit, faPlus } from '@fortawesome/free-solid-svg-icons'
	import { clipboard } from '@skeletonlabs/skeleton'
	import Fa from 'svelte-fa'

	export let data
	$examplePipelineStore = data.examplePipeline

	const addExampleComponent = () => {
		$examplePipelineStore.components = [
			...$examplePipelineStore.components,
			blankComponent($examplePipelineStore.oid, $examplePipelineStore.components.length)
		]
	}

	$: exampleComponentAdded = $examplePipelineStore.components.length > 0
</script>

<svelte:head>
	<title>Documentation</title>
</svelte:head>

<div class="container mx-auto space-y-8">
	<h1 class="h1 scroll-mt-8">Documentation</h1>

	<div class="flex items-start gap-8">
		<div class="space-y-8 md:text-justify leading-normal">
			<!-- Introduction -->
			<div class="space-y-4">
				<h2 class="h2" id="introduction">Introduction</h2>
				<div class="space-y-8">
					<div class="grid xl:grid-cols-2 gap-4">
						<div class="space-y-8">
							<p>
								Automatic analysis of large text corpora is a complex task. This complexity
								particularly concerns the question of time efficiency. Furthermore, efficient,
								flexible, and extensible textanalysis requires the continuous integration of every
								new text analysis tools. Since there are currently, in the area of NLP and
								especially in the application context of UIMA, only very few to no adequate
								frameworks for these purposes, which are not simultaneously outdated or can no
								longer be used for security reasons, this work will present a new approach to fill
								this gap.
							</p>
						</div>
					</div>
				</div>
			</div>

			<hr class="hr" />

			<!-- Pipelines -->
			<div class="space-y-4">
				<h2 class="h2" id="pipeline">Pipeline</h2>
				<div class="space-y-16">
					<div class="grid xl:grid-cols-2 gap-4">
						<div class="space-y-8">
							<p>
								A pipeline is a collection of components or Analysis Engines that can be executed.
								During an analysis process, the components in the pipeline are executed one after
								another annotating documents. Pipelines do not interact with the input data directly
								but build the structure for an NLP workflow.
							</p>
							<p>
								Creating a pipeline with this web-interface can be done in the <a
									class="anchor"
									href="/pipelines/build">Builder</a
								>. It is a three step form that guides you through building a pipeline either from
								<a class="anchor" href="/pipelines/build#top">scratch</a> or using a
								<a class="anchor" href="/pipelines/build#templates">template</a> as the starting point.
							</p>
							<hr class="hr" />
							<p class="blockquote border-primary-500">
								Choosing a template as a starting point copies all predefined settings into a fresh
								pipeline.
							</p>
						</div>
					</div>
					<div class="grid xl:grid-cols-2 gap-4">
						<div class="space-y-8">
							<p>
								In the second step pipeline specific properties like name, description, tags and
								settings can be edited. Only a name is required to proceed but adding a short
								description is recommend to serve as documentation and help others when sharing a
								pipeline.
							</p>
							<p>
								Try adding a tag to the pipeline
								<span class="hidden lg:inline">on the right.</span>
								<span class="lg:hidden inline">below.</span>
							</p>
							{#if $examplePipelineStore.tags.length === 0}
								<p class="self-end">
									No tags added yet. Enter a value in the text field that says Add a tag... and
									press enter.
								</p>
							{:else}
								<div class="flex gap-4 items-center">
									<p>Tags are listed under the text field.</p>
									<Fa icon={faCheck} size="2x" class="text-success-500" />
								</div>
							{/if}
						</div>
						<div>
							<div class="section-wrapper flex flex-col gap-4 p-4">
								<TextInput label="Name" bind:value={$examplePipelineStore.name} />

								<TextInput label="Description" bind:value={$examplePipelineStore.description} />
								<Chips
									label="Tags"
									bind:values={$examplePipelineStore.tags}
								/>
							</div>
						</div>
					</div>
					<div class="grid xl:grid-cols-2 gap-4">
						<div class="space-y-8">
							<p>
								This is the state of your pipeline so far. Since there no components yet, the
								pipeline doesn't really do anything. Let's add some components to the pipeline.
							</p>
							<p class="blockquote border-primary-500">
								A pipeline must have at least one component to be created.
							</p>
						</div>
						<div class="card-fancy grid items-start min-h-[300px]">
							<PipelineCard pipeline={$examplePipelineStore} />
						</div>
					</div>
				</div>
			</div>

			<hr class="hr" />

			<!-- Components -->
			<div class="space-y-4">
				<h2 class="h2" id="component">Component</h2>
				<div class="space-y-16">
					<div class="grid xl:grid-cols-2 gap-4">
						<div class="space-y-8">
							<p>
								Components are the part of DUUI that actually do the processing and therefore offer
								the most settings. When creating a pipeline you can choose from a set of predefined
								components or create your own. Once added to the pipeline, a component can be edited
								by clicking the <Fa icon={faEdit} class="inline" /> icon. This will open a drawer on
								the right, that allows for modification of a component.
							</p>
							<div class="space-y-1">
								<p>Settings include</p>
								<p class="font-bold">Name</p>
								<p>
									<span class="font-bold">Driver</span> - The Driver is responsible for the instantiation
									of a component during a process.
								</p>
								<p>
									<span class="font-bold">Target</span> - The component's target depends on the selected
									driver. For Docker, Kubernetes and Swarm Drivers, the target is the full image name.
									For UIMA it is the class path to the Annotator represented by this component and for
									a Remote Driver the URL has to specified.
								</p>
								<p class="font-bold">Tags</p>
								<p class="font-bold">Description</p>
								<p class="font-bold">Options</p>
								<p class="font-bold">Parameters</p>
							</div>
							<p>
								Options are specific to the selected driver. Most of the time the default options
								are sufficient and modifications are only for special uses cases. Parameters are
								useful if the component requires settings that are not controlled by DUUI.
							</p>
							<hr class="hr" />
							<p class="blockquote border-primary-500">
								When editing a specific pipeline, clicking the <Fa icon={faClone} class="inline" /> icon
								clones the component's settings and prefills the creation form.
							</p>
						</div>
						<div class="grid items-center">
							{#if exampleComponentAdded}
								<div class="space-y-4">
									{#if $exampleComponent.target === ''}
										<p>
											Open the drawer by clicking on the <Fa icon={faEdit} class="inline" /> icon and
											add a target. If you don't have a target at hand, you can choose the DUUIDockerDriver
											and enter a docker image as the target.
										</p>
										<div class="flex justify-center">
											<button
												use:clipboard={'docker.texttechnologylab.org/textimager-duui-spacy-single-de_core_news_sm:latest'}
												class="button-primary">Copy example Image</button
											>
										</div>
									{/if}
									<div class="grid gap-4">
										{#each $examplePipelineStore.components as component}
											<PipelineComponent example={true} {component} />
										{/each}
									</div>
								</div>
							{:else}
								<div class="mx-auto">
									<button class="button-primary" on:click={addExampleComponent}>
										<Fa icon={faPlus} />
										<span>Create component</span>
									</button>
								</div>
							{/if}
						</div>
					</div>
				</div>
			</div>

			<hr class="hr" />

			<!-- Processes -->
			<div class="space-y-4">
				<h2 class="h2" id="process">Process</h2>
				<div class="space-y-16">
					<div class="grid xl:grid-cols-2 gap-4">
						<div class="space-y-4">
							<p>
								A process manages the flow of data and pipeline execution. Starting a process is
								possible on a pipeline page. On the process creation screen you are asked to select
								an input, output and optionally settings that influence the process behavior.
							</p>
							<div class="space-y-4">
								<h3 class="h3">Input and Output</h3>
								<p>
									Any process must be provided with an input source to be started. Each requires
									different properties to be set. The available input sources are:
								</p>
								<ul class="pl-8 space-y-4">
									<li>
										<h4 class="h4">Text</h4>
										<p>
											For simple and quick analysis you can choose to process plain text. The text
											to be analyzed can be entered in a text area.
										</p>
									</li>
									<li>
										<h4 class="h4">File</h4>
										<p>
											Selecting file as the input source allows for the upload of one or multiple
											files.
										</p>
									</li>
									<li>
										<h4 class="h4">Cloud</h4>
										<p>
											There are currently two cloud storage providers available to use: Dropbox and
											Min.io (s3). More will be added in the future. To use your cloud storage
											provider of choice, a connection must be established on your <a
												class="anchor"
												href="/account">Account</a
											> page.
										</p>
									</li>
								</ul>

								<hr class="hr" />

								<p class="blockquote border-primary-500">
									With the exception of text, all input sources require a file extension to be
									selected.
								</p>
							</div>
							<div class="space-y-4">
								<h3 class="h3">Settings</h3>
								<p>
									Settings can be changed for both the input and output. Their main purpose is to
									filter the files that are processed. This can be done by setting a minimum file
									size or ignoring files that may be at the output location.
								</p>
								<p>
									Process related settings include the option to use multiple workers for parallel
									processing or ignoring errors that occur by skipping to next docment instead of
									failing the entire pipeline.
								</p>
								<p class="blockquote border-primary-500">
									Note that the amount of workers or threads that can be used is limited by the
									system!
								</p>
							</div>
						</div>
					</div>
				</div>
			</div>

			<hr class="hr" />

			<!-- Documents -->
			<div class="space-y-4">
				<h2 class="h2" id="document">Document</h2>
				<div class="space-y-16">
					<div class="grid xl:grid-cols-2 gap-4">
						<div class="space-y-4">
							<p>
								The document is the smallest unit in DUUI and holds the data to be processed.
								Additionally documents track metrics about performance, integrity and performed
								annotations. Documents are presented in a table that shows basic information about
								their current state. Clicking on a row opens a more detailed view that includes the
								duration of different stages in the process, annotations that have been made and a
								treemap of said annotations.
							</p>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
</div>

<style>
	h2 {
		scroll-margin-top: 32px;
	}
</style>
