<script lang="ts">
	import { page } from '$app/stores'
	import { helpStore } from '$lib/store'
	import { faQuestion } from '@fortawesome/free-solid-svg-icons'
	import Fa from 'svelte-fa'
</script>

{#if !$helpStore && !$page.url.pathname.endsWith('/')}
	<button
		on:click={() => {
			$helpStore = true
		}}
		class="hidden fixed right-4 bottom-12 variant-filled-primary !aspect-square z-[999] rounded-full  items-center p-6"
	>
		<Fa icon={faQuestion} size="lg" />
	</button>
{/if}
