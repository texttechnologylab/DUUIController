<script lang="ts">
	import { Icon } from 'flowbite-svelte-icons';
	import { page } from '$app/stores';

	export let text: string;
	export let link: string;
	export let iconName: string;
</script>

<!-- svelte-ignore a11y-role-supports-aria-props -->
<li
	class="sidebar__item | hover:bg-blue-500 aria-selected:bg-blue-500 pr-4
	relative after:content-[''] aria-selected:after:bg-white after:absolute after:w-8 after:rotate-45 after:translate-x-1/2 after:h-8 after:right-0 after:top-1/2 after:-translate-y-1/2"
	aria-selected={$page.url.pathname === link ? 'true' : 'false'}
>
	<a
		href={link}
		class="sidebar__item-link | text-lg font-body flex items-center justify-start gap-4 text-black p-4"
	>
		<Icon class="text-white w-6 h-6 mr-4" name={iconName} />
		<span class="text-white">{text}</span>
	</a>
</li>

<style lang="scss">
</style>
