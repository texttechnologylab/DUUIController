<script lang="ts">
	import { helpStore } from '$lib/store'
	import { faQuestion } from '@fortawesome/free-solid-svg-icons'
	import Fa from 'svelte-fa'
</script>

{#if !$helpStore}
	<div class=" z-100 !rounded-full">
		<button
			on:click={() => ($helpStore = true)}
			class="animate-text animate-underline rounded-none flex items-center gap-2"
		>
			<span>Help</span>
		</button>
	</div>
{/if}
