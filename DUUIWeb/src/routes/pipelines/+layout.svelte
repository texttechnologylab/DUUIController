<script lang="ts">
	import { scrollIntoView } from '$lib/duui/utils/ui'
	import { onMount } from 'svelte'

	onMount(() => {
		scrollIntoView('top')
	})
</script>

<div id="top" class="h-full p-4 space-y-4 scroll-mt-4">
	<slot />
</div>
