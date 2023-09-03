<script lang="ts">
	import { Icon } from 'flowbite-svelte-icons';
	import { goto } from '$app/navigation';
	import type { DUUIPipeline } from '$lib/data';
	import { currentPipelineStore } from '$lib/store';

	export let pipeline: DUUIPipeline;

	const navigateToEdit = () => {
		$currentPipelineStore = pipeline;
		goto('/pipelines/edit');
	};
</script>

<div class="flex flex-col shadow-md relative">
	<header class="flex items-center justify-between bg-blue-500 text-white">
		{#if pipeline.status === 'Completed'}
			<button
				class="absolute top-0 left-0
			 hover:bg-green-600 bg-green-500
			  rounded-full p-2 -translate-x-[20%] -translate-y-[60%]"
			>
				<Icon name="check-solid" class="w-6 h-6" />
			</button>
		{:else if pipeline.status === 'Error'}
			<button
				class="absolute top-0 left-0
			 hover:bg-red-600 bg-red-500
			  rounded-full p-2 -translate-x-[20%] -translate-y-[60%]"
			>
				<Icon name="bell-active-solid" class="w-6 h-6" />
			</button>
		{/if}
		<p class="p-4 grow font-heading font-bold">{pipeline.name}</p>
		<button class="p-4 hover:bg-blue-600" on:click={navigateToEdit}>
			<Icon name="edit-outline" />
		</button>
	</header>

	<div class="grid grid-cols-2 gap-8 p-4">
		<p>{pipeline.components.length} Components</p>
		<p class="text-right">{pipeline.status}</p>
	</div>
</div>
