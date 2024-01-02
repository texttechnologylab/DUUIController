<script lang="ts">
	import Documentation from '$lib/svelte/widgets/navigation/Documentation.svelte'
</script>

<div class="flex-auto flex">
	<Documentation />
	<div class="relative p-8 scroll-smooth border-l border-l-surface-400/20">
		<slot />
	</div>
</div>
