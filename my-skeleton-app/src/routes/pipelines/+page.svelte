<script lang="ts">
	import { faPlus } from '@fortawesome/free-solid-svg-icons';
	import {
		Avatar,
		ProgressBar,
		Table,
		tableMapperValues,
		type TableSource
	} from '@skeletonlabs/skeleton';
	import Fa from 'svelte-fa';

	import { popup } from '@skeletonlabs/skeleton';
	import type { PopupSettings } from '@skeletonlabs/skeleton';

	// const sourceData = [
	// 	{ position: 1, name: 'Hydrogen', weight: 1.0079, symbol: 'H' },
	// 	{ position: 2, name: 'Helium', weight: 4.0026, symbol: 'He' },
	// 	{ position: 3, name: 'Lithium', weight: 6.941, symbol: 'Li' },
	// 	{ position: 4, name: 'Beryllium', weight: 9.0122, symbol: 'Be' },
	// 	{ position: 5, name: 'Boron', weight: 10.811, symbol: 'B' }
	// ];

	export let data;

	let { pipelines } = data;
	let tableData = pipelines.map((pipeline) => {
		return {
			...pipeline,
			size: pipeline.components.length
		};
	});

	let tableSimple: TableSource = {
		// A list of heading labels.
		head: ['Name', 'Status', 'Size'],
		// The data visibly shown in your table body UI.
		body: tableMapperValues(tableData, ['name', 'status', 'size']),
		// Optional: The data returned when interactive is enabled and a row is clicked.
		meta: tableMapperValues(tableData, ['name', 'status', 'size']),
		// Optional: A list of footer labels.
		foot: ['Total', '', `<code class="code">${pipelines.length} Pipelines</code>`]
	};

	function mySelectionHandler(e: CustomEvent<string[]>): void {
		console.log(e.detail);
	}
</script>

<div class="p-8 flex flex-col gap-4">
	<div>
		<a href="pipelines/new" class="btn variant-filled-primary">
			<span>Create new</span>
			<Fa icon={faPlus} />
		</a>
	</div>

	<Table source={tableSimple} interactive={true} on:selected={mySelectionHandler} />
	<ProgressBar />
</div>
