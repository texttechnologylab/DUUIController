<script lang="ts">
	import DocumentationNav from '$lib/components/SidebarNav.svelte';
</script>

<div class="flex-auto w-full h-full flex overflow-hidden">
	<DocumentationNav hidden={true} />
	<slot />
</div>
