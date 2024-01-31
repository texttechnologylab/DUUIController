<script lang="ts">
	import Endpoint from '$lib/svelte/Endpoint.svelte'

	export let data
	const { endpoints } = data
</script>

<div class="bg-repeat gradient md:text-left py-8">
	<div class=" max-w-7xl mx-auto space-y-8 p-4">
		<h1 class="h1 font-bold scroll-mt-4 !mb-16" id="rest">API Reference</h1>
		<div class="space-y-4 text-start">
			<h2 class="h2 font-bold pb-4">REST</h2>
			<div class=" space-y-8">
				<p>The DUUI REST Api consists of 3 reachable endpoint</p>
				<div class="flex gap-4 items-center justify-start">
					{#each ['Pipeline', 'Component', 'Process'] as endpoint}
						<a class="anchor" href="/documentation/api#{endpoint.toLowerCase()}">{endpoint}</a>
					{/each}
				</div>
			</div>
			<h3 class="h3 font-bold pt-8 scroll-mt-4" id="pipeline">Pipelines</h3>
			{#each endpoints.pipelines as endpoint}
				<Endpoint {endpoint} />
			{/each}

			<h3 class="h3 font-bold pt-8 scroll-mt-4" id="component">Components</h3>
			{#each endpoints.components as endpoint}
				<Endpoint {endpoint} />
			{/each}

			<h3 class="h3 font-bold pt-8 scroll-mt-4" id="process">Processes</h3>

			{#each endpoints.processes as endpoint}
				<Endpoint {endpoint} />
			{/each}
		</div>
		<hr class="hr !h-[2px] !my-16" />

		<div class="space-y-4 text-justify">
			<h2 class="h2 font-bold pb-4 scroll-mt-4" id="java">Java</h2>
			<p>
				Lorem ipsum dolor sit amet consectetur adipisicing elit. Ducimus quidem perspiciatis
				debitis, animi amet minima alias architecto molestiae saepe sit veritatis maiores dolorum?
				Repudiandae, architecto ea! Fugit natus praesentium suscipit.
			</p>
		</div>
		<hr class="hr !h-[2px] !my-16" />

		<div class="space-y-4 text-justify">
			<h2 class="h2 font-bold pb-4 scroll-mt-4" id="python">Python</h2>
			<p>
				Lorem ipsum dolor sit amet consectetur adipisicing elit. Ducimus quidem perspiciatis
				debitis, animi amet minima alias architecto molestiae saepe sit veritatis maiores dolorum?
				Repudiandae, architecto ea! Fugit natus praesentium suscipit.
			</p>
		</div>
	</div>
</div>
