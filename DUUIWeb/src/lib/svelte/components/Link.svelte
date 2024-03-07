<!-- @component A component that represents an anchor in the mobile navigation. -->

<script lang="ts">
	import { getDrawerStore } from '@skeletonlabs/skeleton'

	export let href: string
	export let dimmed: boolean = false
	export let underline: boolean = false
	const drawerStore = getDrawerStore()
</script>

<a
	on:click={() => drawerStore.close()}
	class="hover:!text-primary-500 transition-300 text-start
		   animate-underline
			{dimmed ? 'dimmed' : ''}
			{underline ? 'anchor' : ''}"
	{href}
>
	<slot />
</a>
