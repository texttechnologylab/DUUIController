<script lang="ts">
	import { Icon } from 'flowbite-svelte-icons';
	import { page } from '$app/stores';

	export let text: string;
	export let href: string;
	export let iconName: string;
</script>

<!-- svelte-ignore a11y-role-supports-aria-props -->
<li class="list-none">
	<a
		{href}
		class="
		text-md font-body
		flex items-center justify-center gap-6
		px-4 py-3
		text-white
	 	aria-selected:bg-blue-600 hover:bg-blue-600 transition-color duration-300
		rounded-full
		 "
		aria-selected={$page.url.pathname === href ? 'true' : 'false'}
	>
		<Icon class=" min-w-[1.25rem] aspect-square" name={iconName} />
		<span class="">{text}</span>
	</a>
</li>
