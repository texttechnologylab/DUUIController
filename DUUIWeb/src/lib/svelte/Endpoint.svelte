<script lang="ts">
	import { faChevronDown } from '@fortawesome/free-solid-svg-icons'
	import { CodeBlock } from '@skeletonlabs/skeleton'
	import Fa from 'svelte-fa'

	export let endpoint: APIEndpoint
	export let expanded: boolean = false
</script>

<div class="endpoint">
	<div class="flex gap-8 items-center p-4 {expanded ? 'bg-surface-200-700-token' : ''}">
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
	<div class:open={expanded} class="content dimmed">
		<div class="content-wrapper">
			<div>
				<div class="p-4 border-t border-color">
					<p class="max-w-full">{endpoint.description}</p>
				</div>
				{#if endpoint.exampleRequest}
					<div class="p-4 border-t border-color space-y-4">
						<h4 class="h4">Example Request</h4>
						<CodeBlock language="ts" code={endpoint.exampleRequest} />
					</div>
				{/if}
				{#if endpoint.returns}
					<div class="p-4 border-t border-color space-y-4">
						<h4 class="h4">Responses</h4>
						<p>All responses are returned as a JSON String.</p>
						<div class="flex flex-wrap gap-2 items-start">
							{#each endpoint.returns as response}
								<p class="badge variant-ghost-primary">{response.code} - {response.message}</p>
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
