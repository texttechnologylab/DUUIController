<script lang="ts">
	import { faCheck } from '@fortawesome/free-solid-svg-icons'
	import Fa from 'svelte-fa'

	export let text: string
	export let checked: boolean = false
</script>

<button
	class="btn variant-ghost-{checked
		? 'success'
		: 'surface'} p-4 rounded-md shadow-lg flex justify-between items-center text-left"
	on:click={() => (checked = !checked)}
>
	<span>{text}</span>
	{#if checked}
		<Fa icon={faCheck} size="lg" />
	{/if}
</button>
