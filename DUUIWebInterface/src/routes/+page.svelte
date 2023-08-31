<script lang="ts">
	import { getPipeline, getPipelineStatus, getPipelines } from '../requests/get';

	let promise: Promise<string | Object>;
	let promiseAll: Promise<string | Object>;
</script>

<h1 class="text-center text-4xl font-bold p-4">Welcome to DUUI</h1>
<div class="grid grid-cols-2 gap-4 items-center justify-center">
	<button
		class="bg-red-400 text-white p-4 shadow-md rounded-sm"
		on:click={() => (promise = getPipeline('1234'))}>Get One</button
	>
	{#await promise}
		<p>Waiting</p>
	{:then pipeline}
		<p>{pipeline}</p>
	{/await}
	<button
		class="bg-red-400 text-white p-4 shadow-md rounded-sm"
		on:click={() => (promiseAll = getPipelines())}>Get All</button
	>
	{#await promiseAll}
		<p>Waiting</p>
	{:then pipeline}
		<p>{pipeline}</p>
	{/await}
</div>
