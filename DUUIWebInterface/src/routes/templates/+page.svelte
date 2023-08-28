<script lang="ts">
	import { onMount } from 'svelte';
	import type { DUUIPipeline } from '../../Interfaces/interfaces';
	import DriverIcon from '../../components/DriverIcon.svelte';
	import ToggleButton from '../../components/Buttons/ToggleButton.svelte';
	import { Icon } from 'flowbite-svelte-icons';
	import { editedPipeline } from '../../Interfaces/store';

	let pipelines: DUUIPipeline[] = [];
	onMount(() => {
		fetch(`http://127.0.0.1:9090/pipelines`, {
			method: 'GET',
			mode: 'cors'
		})
			.then((response) => response.text())
			.then((data) => {
				pipelines = JSON.parse(data).pipelines;
			})
			.catch((error) => {
				console.log(error);
			});
	});

	let expanded: boolean[] = pipelines.map(() => false);
</script>

<div class="flex items-start justify-start gap-8">
	{#each pipelines as pipeline, index}
		<div class="flex flex-col gap-2 p-4 border-[1px] border-slate-800">
			<div class="flex gap-4 items-center">
				<p class="grow">{pipeline.name}</p>
				<p>{pipeline.components.length} Components</p>
				<ToggleButton
					on:expand={() => (expanded[index] = true)}
					on:collapse={() => (expanded[index] = false)}
				/>
			</div>
			{#if expanded[index]}
				{#each pipeline.components as component}
					<div class="px-4 flex gap-4 items-center">
						<DriverIcon driver={component.driver} />
						<p class="text-lg">{component.name}</p>
					</div>
				{/each}
				<button
					class="flex items-end grow justify-between p-4 pt-8 border-t-[1px] border-t-slate-800"
					on:click={() => ($editedPipeline = pipeline)}
				>
					<a href="/analysis/edit" class="text-lg">Edit Pipeline</a>
					<Icon name="angle-right-solid" class="w-6 h-6" />
				</button>
			{/if}
		</div>
	{/each}
</div>
