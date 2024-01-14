<script lang="ts">
	import { scrollIntoView } from '$lib/duui/utils/ui'
	import HelpToggle from '$lib/svelte/widgets/navigation/HelpToggle.svelte'
	import { onMount } from 'svelte'

	onMount(() => {
		scrollIntoView('top')
	})
</script>

<div id="top" class="scroll-mt-4 h-full relative">
	<slot />
</div>
