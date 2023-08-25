<script lang="ts">
	export let options: string[];
	export let label: string;
	export let value: string = '';
</script>

<div class="grid gap-2">
	<p>{label}</p>

	<select
		class="p-3 border-[1px] text-md border-slate-800 bg-white text-slate-800 cursor-pointer"
		id="driver"
		bind:value
	>
		{#each options as option}
			<option value={option}>{option}</option>
		{/each}
	</select>
</div>
