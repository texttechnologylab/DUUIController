<script lang="ts">
	import { scrollIntoView } from '$lib/duui/utils/ui'
	import { onMount } from 'svelte'

	onMount(() => {
		scrollIntoView('top')
	})
</script>

<div id="top" class="h-full">
	<slot />
</div>
