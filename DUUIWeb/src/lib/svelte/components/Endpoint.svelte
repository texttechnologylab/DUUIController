<!-- @component A component that represents an API endpoint. -->
<script lang="ts">
	import { faChevronDown } from '@fortawesome/free-solid-svg-icons'
	import { CodeBlock } from '@skeletonlabs/skeleton'
	import Fa from 'svelte-fa'

	export let endpoint: APIEndpoint
	export let expanded: boolean = false
</script>

<div class="endpoint">
	<div class="flex gap-8 items-center p-4 bg-surface-50-900-token">
		<p class="font-bold text-lg basis-16">{endpoint.method}</p>
		<p>{endpoint.route}</p>
		<button
			class="ml-auto transition-transform duration-300"
			class:turn={expanded}
			on:click={() => (expanded = !expanded)}
		>
			<Fa icon={faChevronDown} size="lg" />
		</button>
	</div>
	<div class:open={expanded} class="content bg-surface-100-800-token">
		<div class="content-wrapper">
			<div>
				<div class="space-y-4 p-4 border-t border-color">
					<p>{endpoint.description}</p>
				</div>

				{#if endpoint.parameters && endpoint.parameters.length > 0}
					<div class="p-4 space-y-2 border-t border-color">
						<h4 class="h3">Parameters</h4>
						<div class="grid md:grid-cols-2 gap-4">
							{#each endpoint.parameters as param}
								<div class="space-y-1">
									<div class="flex items-center gap-2 justify-start">
										<p class="text-xs text-surface-600-300-token">
											{param.type}
										</p>
										<p class="font-bold text-lg">{param.name}</p>
									</div>
									<p class="text-sm text-surface-600-300-token">{param.description}</p>
								</div>
							{/each}
						</div>
					</div>
				{/if}

				{#if endpoint.exampleRequest}
					<div class="p-4 border-t border-color space-y-4">
						<h4 class="h3">Example Request</h4>
						<CodeBlock language="ts" code={endpoint.exampleRequest} />
					</div>
				{/if}
				{#if endpoint.returns}
					<div class="p-4 border-t border-color space-y-4">
						<h4 class="h3">Responses</h4>
						<p>All responses are returned as a JSON String.</p>
						<div class="flex flex-wrap gap-2 items-start">
							{#each endpoint.returns as response}
								<p class="tag">{response.code} - {response.message}</p>
							{/each}
						</div>
					</div>
				{/if}
			</div>
		</div>
	</div>
</div>

<style>
	.content-wrapper {
		overflow: hidden;
	}

	.content {
		display: grid;
		grid-template-rows: 0fr;
		transition: grid-template-rows 300ms;
	}

	.open {
		grid-template-rows: 1fr;
	}

	.turn {
		transform: rotate(180deg);
	}
</style>
