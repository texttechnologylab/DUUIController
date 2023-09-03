<script lang="ts">
	export let label: string;
	export let value: string = '';
</script>

<div class="grid gap-2">
	<p>{label}</p>
	<input
		bind:value
		type="password"
		class="p-2 text-md border-slate-800 bg-white text-slate-800 border-[1px]"
	/>
</div>
