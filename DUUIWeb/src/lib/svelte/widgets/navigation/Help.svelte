<script lang="ts">
	import { helpStore } from '$lib/store'
	import { faClose } from '@fortawesome/free-solid-svg-icons'
	import Fa from 'svelte-fa'
</script>

<div class="hidden lg:block p-8 max-w-[50ch] scroll-smooth space-y-4 [&>h3]:scroll-mt-4">
	<div class="flex items-center justify-between gap-4">
		<h2 class="h2">Help</h2>
		<button on:click={() => ($helpStore = false)}>
			<Fa icon={faClose} size="2x"/>
		</button>
	</div>

	<ul class="section-wrapper p-4">
		<li><a class="anchor" href="#pipeline">Pipelines</a></li>
		<li><a class="anchor" href="#component">Component</a></li>
		<li><a class="anchor" href="#editor">Editor</a></li>
		<li><a class="anchor" href="#process">Process</a></li>
		<li><a class="anchor" href="#document">Documents</a></li>
	</ul>

	<h3 class="h3" id="pipeline">Pipeline</h3>
	<p class="max-w-[60ch]">
		Lorem ipsum dolor sit amet consectetur adipisicing elit. Blanditiis ipsa, porro sit quidem
		reiciendis eum. Molestias placeat accusamus quod aliquid natus cupiditate suscipit consectetur
		ea minus, sequi fugit, quisquam ipsa?
	</p>

	<h3 class="h3" id="component">Component</h3>
	<p class="max-w-[60ch]">
		Lorem ipsum dolor sit amet consectetur adipisicing elit. Blanditiis ipsa, porro sit quidem
		reiciendis eum. Molestias placeat accusamus quod aliquid natus cupiditate suscipit consectetur
		ea minus, sequi fugit, quisquam ipsa?
	</p>

	<h3 class="h3" id="editor">Editor</h3>
	<p class="max-w-[60ch]">
		Lorem ipsum dolor sit amet consectetur adipisicing elit. Blanditiis ipsa, porro sit quidem
		reiciendis eum. Molestias placeat accusamus quod aliquid natus cupiditate suscipit consectetur
		ea minus, sequi fugit, quisquam ipsa?
	</p>

	<h3 class="h3" id="process">Process</h3>
	<p class="max-w-[60ch]">
		Lorem ipsum dolor sit amet consectetur adipisicing elit. Blanditiis ipsa, porro sit quidem
		reiciendis eum. Molestias placeat accusamus quod aliquid natus cupiditate suscipit consectetur
		ea minus, sequi fugit, quisquam ipsa?
	</p>

	<h3 class="h3" id="document">Documents</h3>
	<p class="max-w-[60ch]">
		Lorem ipsum dolor sit amet consectetur adipisicing elit. Blanditiis ipsa, porro sit quidem
		reiciendis eum. Molestias placeat accusamus quod aliquid natus cupiditate suscipit consectetur
		ea minus, sequi fugit, quisquam ipsa?
	</p>
</div>
