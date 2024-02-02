<script lang="ts">
	import { scrollIntoView } from '$lib/duui/utils/ui'
	import { onMount } from 'svelte'

	onMount(() => {
		scrollIntoView('top')
	})
</script>

<div id="top" class="scroll-mt-4">
	<div class="p-4 md:p-8 gradient bg-repeat">
		<slot />
	</div>
</div>
