<script lang="ts">
	import Documentation from '$lib/svelte/widgets/navigation/Documentation.svelte'
</script>

<div class="relative h-full grid items-start">
	<div class="md:flex">
		<Documentation />
		<div class="p-8 border-l border-l-surface-400/20">
			<slot />
		</div>
	</div>
</div>
