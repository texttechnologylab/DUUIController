<script lang="ts">
	import { blankComponent } from '$lib/duui/component'
	import { currentPipelineStore, exampleComponent } from '$lib/store.js'
	import Chips from '$lib/svelte/components/Chips.svelte'
	import PipelineCard from '$lib/svelte/components/PipelineCard.svelte'
	import PipelineComponent from '$lib/svelte/components/PipelineComponent.svelte'
	import TextInput from '$lib/svelte/components/TextInput.svelte'
	import { faArrowUp, faCheck, faClone, faEdit, faPlus } from '@fortawesome/free-solid-svg-icons'
	import { clipboard } from '@skeletonlabs/skeleton'
	import Fa from 'svelte-fa'

	export let data
	$currentPipelineStore = data.examplePipeline
	let exampleComponentAdded: boolean = false

	const addExampleComponent = () => {
		exampleComponentAdded = true
		$currentPipelineStore.components = [
			...$currentPipelineStore.components,
			blankComponent($currentPipelineStore.oid, $currentPipelineStore.components.length)
		]
	}
</script>

<svelte:head>
	<title>Documentation</title>
</svelte:head>

<div class=" max-w-7xl mx-auto space-y-8">
	<h1 class="h1 scroll-mt-8">Documentation</h1>
	<hr class="hr !w-full" />

	<div class="space-y-8 md:text-justify leading-normal">
		<!-- Introduction -->
		<div class="space-y-4">
			<h2 class="h2" id="introduction">Introduction</h2>
			<p>
				Automatic analysis of large text corpora is a complex task. This complexity particularly
				concerns the question of time efficiency. Furthermore, efficient, flexible, and extensible
				textanalysis requires the continuous integration of every new text analysis tools. Since
				there are currently, in the area of NLP and especially in the application context of UIMA,
				only very few to no adequate frameworks for these purposes, which are not simultaneously
				outdated or can no longer be used for security reasons, this work will present a new
				approach to fill this gap.
			</p>
		</div>

		<hr class="hr !w-full" />

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
								href="/pipelines/editor">Editor</a
							>. It is a three step form that guides you through building a pipeline either from
							<a class="anchor" href="/pipelines/editor#top">scratch</a> or using a
							<a class="anchor" href="/pipelines/editor#templates">template</a> as the starting point.
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
						{#if $currentPipelineStore.tags.length === 0}
							<p class="self-end">
								No tags added yet. Enter a value in the text field that says Add a tag... and press
								enter.
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
							<TextInput label="Name" bind:value={$currentPipelineStore.name} />

							<TextInput label="Description" bind:value={$currentPipelineStore.description} />
							<Chips
								label="Tags"
								placeholder="Add a tag..."
								bind:values={$currentPipelineStore.tags}
							/>
						</div>
					</div>
				</div>
				<div class="grid xl:grid-cols-2 gap-4">
					<div class="space-y-8">
						<p>
							This is the state of your pipeline so far. Since there no components yet, the pipeline
							doesn't really do anything. Let's add some components to the pipeline.
						</p>
						<p class="blockquote border-primary-500">
							A pipeline must have at least one component to be created.
						</p>
					</div>
					<div class="card-fancy grid items-start min-h-[300px]">
						<PipelineCard pipeline={$currentPipelineStore} />
					</div>
				</div>
			</div>
		</div>

		<hr class="hr !w-full" />

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
							by clicking the <Fa icon={faEdit} class="inline" /> icon. This will open a drawer on the
							right, that allows for modification of a component.
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
							Options are specific to the selected driver. Most of the time the default options are
							sufficient and modifications are only for special uses cases. Parameters are useful if
							the component requires settings that are not controlled by DUUI.
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
										Open the drawer by clicking on the <Fa icon={faEdit} class="inline" /> icon and add
										a target. If you don't have a target at hand, you can choose the DUUIDockerDriver
										and enter
										<button
											use:clipboard={'docker.texttechnologylab.org/textimager-duui-spacy-single-de_core_news_sm:latest'}
											class="button-surface my-2"
											>docker.texttechnologylab.org/textimager-duui-spacy-single-de_core_news_sm:latest</button
										> as the target.
									</p>
								{/if}
								{#each $currentPipelineStore.components as component}
									<PipelineComponent example={true} {component} />
								{/each}
								<button class="button-primary" on:click={addExampleComponent}>
									<Fa icon={faPlus} />
									<span>Create component</span>
								</button>
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

		<hr class="hr !w-full" />

		<!-- Processes -->
		<div class="space-y-4">
			<h2 class="h2" id="process">Process</h2>
			<div class="space-y-4">
				<p>
					A process manages data and pipeline execution. Starting a process is possible on a
					pipeline page from the top right.
				</p>
				<p>
					On the process creation screen you are asked to select an input, output and optionally
					settings the influence the process' behavior. There are currently for input options that
					each have different settings.
				</p>
				<div class="space-y-2">
					<div>
						<h4 class="h4">Text</h4>
						<p>No special settings are necessary here as the text is directly available to DUUI.</p>
					</div>
					<div>
						<h4 class="h4">File</h4>
						<p>
							Asks you to upload one or more files to DUUI, that are then processed. A file
							extension (.txt, .xmi or .gz) must be selected.
						</p>
					</div>
					<div>
						<h4 class="h4">Dropbox</h4>
						<p>
							Afterwards, loading files from Dropbox requires a file extension (.txt, .xmi or .gz)
							and the path to a folder.
						</p>
					</div>
					<div>
						<h4 class="h4">Min.io</h4>
						<p>
							Loading files from Min.io is similar to Dropbox. Instead of a path to a folder, you
							must specify the location to load from as 'bucket/path/to/folder'.
						</p>
					</div>
				</div>
				<p>
					To use Dropbox and Min.io you first have to connect your Dropbox account with DUUI on your <a
						class="anchor"
						href="/account">Account</a
					> page. Both Dropbox and Min.io allow for recursive search, a minimum file size and sorting
					in ascending order.
				</p>
			</div>
		</div>

		<hr class="hr !w-full" />

		<!-- Documents -->
		<div class="space-y-4 text-justify">
			<h2 class="h2" id="document">Document</h2>
			<div class="space-y-4">
				<p>
					Lorem ipsum dolor sit amet consectetur adipisicing elit. Ducimus quidem perspiciatis
					debitis, animi amet minima alias architecto molestiae saepe sit veritatis maiores dolorum?
					Repudiandae, architecto ea! Fugit natus praesentium suscipit.
				</p>
			</div>
		</div>
	</div>
</div>

<a
	href="/documentation#introduction"
	class="button-neutral rounded-full fixed bottom-8 right-8 z-[20]"
>
	<Fa icon={faArrowUp} size="lg" />
</a>

<style>
	h2 {
		scroll-margin-top: 32px;
	}
</style>
