<script lang="ts">
	import { getDrawerStore } from '@skeletonlabs/skeleton'

	export let href: string
	export let dimmed: boolean = false
	export let underline: boolean = false
	const drawerStore = getDrawerStore()
</script>

<!-- TODO: CLOSING ON EVERY LINK CLICK IS NOT OPTIMAL -->

<a
	on:click={() => drawerStore.close()}
	class="hover:text-primary-500 transition-colors
		   animate-underline
			{dimmed ? 'text-surface-400 dark:text-surface-200' : ''}
			{underline ? 'anchor' : ''}"
	{href}
>
	<slot />
</a>
