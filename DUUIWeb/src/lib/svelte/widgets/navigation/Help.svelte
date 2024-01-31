<script lang="ts">
	import { helpStore } from '$lib/store'
	import { faAngleDoubleRight, faClone, faEdit } from '@fortawesome/free-solid-svg-icons'
	import Fa from 'svelte-fa'
</script>

<div class:open={$helpStore} class="w-full scroll-smooth text-lg leading-relaxed">
	<div
		class="sticky top-0 bg-surface-50-900-token p-4 flex items-center justify-start gap-4 border-b border-color"
	>
		<button
			class="rounded-full p-3 aspect-square items-center"
			on:click={() => {
				$helpStore = false
			}}
		>
			<Fa icon={faAngleDoubleRight} size="lg" />
		</button>
		<h2 class="h2">Help</h2>
		<ul class="ml-auto flex gap-4 flex-wrap">
			<li><a class="anchor" href="#pipeline">Pipelines</a></li>
			<li><a class="anchor" href="#component">Component</a></li>
			<li><a class="anchor" href="#process">Process</a></li>
		</ul>
	</div>

	<div class="p-4 space-y-8 [&>a]:anchor">
		<div class="space-y-4">
			<h3 class="h3" id="pipeline">Pipeline</h3>
			<div class="space-y-4">
				<p>To create a Pipeline visit the <a class="anchor" href="/pipelines/editor">Editor</a>.</p>
				<ol class="list-decimal pl-8">
					<li>
						Chose a starting point:
						<ul class="pl-4 gap-4 flex">
							<li><a class="anchor" href="/pipelines/editor#top">Start from Scratch</a></li>
							<li>
								Start from a <a class="anchor" href="/pipelines/editor#templates">Templates</a>
							</li>
						</ul>
					</li>
					<li>Give your Pipeline a name.</li>
					<li>Optionally give it a description and add a few tags for classification.</li>
					<li>Settings are work in progress and can be ignored for now</li>
					<li>Click the button that says Components on the top right.</li>
					<li>
						Add at least one <a class="anchor" href="#component">Component</a> and then click finish
						on the top right again.
					</li>
				</ol>
				<p>
					A Pipeline can have one or more Components that each can be customized further. You can
					always add, edit and remove components and pipelines from the dashboard at <a
						class="anchor"
						href="/pipelines">Pipelines</a
					>.
				</p>
				<hr class="hr" />
				<p>
					In the future settings may include the option to add a timeout after which a pipeline is
					cancelled automatically when components stop responding or how the pipeline behaves when
					errors occur.
				</p>
			</div>
		</div>

		<div class="space-y-4">
			<h3 class="h3" id="component">Component</h3>
			<div class="space-y-4">
				<p>
					Components are the part of DUUI that actually do the processing and therefore offer the
					most settings. When creating a pipeline you can choose from a set of predefined
					components. Once added to the pipeline, a component can be edited by clicking the <Fa
						icon={faEdit}
						class="inline"
					/> icon. This will open a drawer on the right, that allows for modification of a component.
				</p>
				<p>Settings include</p>
				<ul class="list-disc pl-8">
					<li>Name</li>
					<li>Driver</li>
					<li>Target</li>
					<li>Tags</li>
					<li>Description</li>
					<li>Options</li>
					<li>Parameters</li>
				</ul>
				<p>
					Options are specific to the selected driver. Most of the time the default options are
					sufficient and modifications are only for special uses cases. Parameters are useful if the
					component requires settings that are not controlled by DUUI.
				</p>
				<hr class="hr" />
				<p>
					When editing a specific pipeline, clicking the <Fa icon={faClone} class="inline" /> icon clones
					the component's settings and prefills the creation form.
				</p>
			</div>
		</div>

		<div class="space-y-4">
			<h3 class="h3" id="process">Process</h3>
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
				<ul class="list-disc pl-8 space-y-2">
					<li>
						<h4 class="h4">Text</h4>
						<p>No special settings are necessary here as the text is directly available to DUUI.</p>
					</li>
					<li>
						<h4 class="h4">File</h4>
						<p>
							Asks you to upload one or more files to DUUI, that are then processed. A file
							extension (.txt, .xmi or .gz) must be selected.
						</p>
					</li>
					<li>
						<h4 class="h4">Dropbox</h4>
						<p>
							Afterwards, loading files from Dropbox requires a file extension (.txt, .xmi or .gz)
							and the path to a folder.
						</p>
					</li>
					<li>
						<h4 class="h4">Min.io</h4>
						<p>
							Loading files from Min.io is similar to Dropbox. Instead of a path to a folder, you
							must specify the location to load from as 'bucket/path/to/folder'.
						</p>
					</li>
				</ul>
				<p>
					To use Dropbox and Min.io you first have to connect your Dropbox account with DUUI on your <a
						class="anchor"
						href="/account">Account</a
					> page. Both Dropbox and Min.io allow for recursive search, a minimum file size and sorting
					in ascending order.
				</p>
			</div>
		</div>
	</div>
</div>

<style>
	h3 {
		scroll-margin-top: 8rem;
	}
</style>