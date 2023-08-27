<script lang="ts">
	import { Icon } from 'flowbite-svelte-icons';

	export let template: Template;
	let drivers: string[] = [];

	import Dialog from './Dialog.svelte';
	let cancelDialog: HTMLDialogElement;

	$: pipelineView = true;
	template.components.forEach((element) => {
		if (drivers.indexOf(element.driver) === -1) drivers.push(element.driver);
	});

	$: countComponents = (driverName: string) => {
		return template.components.filter((component, index, arr) => component.driver === driverName)
			.length;
	};

	async function build() {
		fetch('http://127.0.0.1:9090/build', {
			method: 'POST',
			headers: {
				'Content-Type': 'application/xml',
				Accept: 'application/json'
			},
			body: JSON.stringify(template)
		})
			.then((response) => response.text())
			.then((xml) => {
				console.log(xml);
			})
			.catch((error) => {
				console.error('Error:', error);
			});
	}

	async function cancelPipeline() {
		fetch('http://127.0.0.1:9090/cancel/' + template.name, {
			method: 'POST'
		})
			.then((response) => response.text())
			.then((xml) => {
				console.log(xml);
			})
			.catch((error) => {
				console.error('Error:', error);
			});
	}
</script>

<div class=" bg-slate-100 rounded-md max-w-lg overflow-hidden">
	<h2 class="col-span-2 text-center text-white text-xl bg-slate-700 p-8">
		{template.name}
	</h2>
	<div class="py-4 relative">
		{#if pipelineView}
			<div class="px-4 pb-8 grid grid-cols-pipelineItem gap-4">
				{#each template.components as component, index}
					<p>{index + 1}</p>
					<p>{component.driver}</p>
					<p>{component.name}</p>
				{/each}
			</div>
		{:else}
			<div class="px-8 pb-8 flex flex-col gap-4">
				{#each drivers as driver}
					<div class="grid grid-cols-2">
						<p>{driver}</p>
						<p class="text-right">{countComponents(driver)} Components</p>
					</div>
				{/each}
			</div>
		{/if}
		<div class="grid grid-cols-3">
			<button
				class="p-4 bg-slate-700 text-white rounded-md m-4 hover:bg-slate-600 transition-colors"
				on:click={build}>Build</button
			>
			<button
				class="p-4 bg-slate-700 text-white rounded-md m-4 hover:bg-slate-600 transition-colors"
				on:click={() => cancelDialog.showModal()}>Cancel</button
			>
			<button
				class="p-4 bg-slate-700 text-white rounded-md m-4 hover:bg-slate-600 transition-colors flex items-center justify-center"
				on:click={() => (pipelineView = !pipelineView)}
			>
				<Icon name={pipelineView ? 'angle-up-solid' : 'angle-down-solid'} class="w-5 h-5" />
			</button>
		</div>
	</div>
	<Dialog bind:dialog={cancelDialog} on:accept={cancelPipeline} />
</div>
