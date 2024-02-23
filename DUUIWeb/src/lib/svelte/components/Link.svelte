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
	class="hover:text-primary-500 transition-300 text-start
		   animate-underline
			{dimmed ? 'dimmed' : ''}
			{underline ? 'anchor' : ''}"
	{href}
>
	<slot />
</a>
