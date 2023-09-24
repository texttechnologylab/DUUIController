<script lang="ts">
	export let name: string
	export let placeholder: string
	export let text: string
</script>

<label class="label grow col-span-2">
	<span>{name}</span>
	<input
		bind:value={text}
		class="input focus-within:outline-primary-400"
		{placeholder}
		type="text"
	/>
</label>
