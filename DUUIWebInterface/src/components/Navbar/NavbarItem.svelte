<script lang="ts">
	import { Icon } from 'flowbite-svelte-icons';
	import { page } from '$app/stores';

	export let text: string;
	export let href: string;
	export let iconName: string;
</script>

<!-- svelte-ignore a11y-role-supports-aria-props -->
<li class="navbar__item">
	<a
		{href}
		class="navbar__item-link
		 text-md font-body flex items-center justify-start gap-6 text-black p-4 px-6
		 hover:bg-slate-500 aria-selected:bg-slate-500 transition-colors"
		aria-selected={$page.url.pathname === href ? 'true' : 'false'}
	>
		<Icon class="text-white w-5 h-5" name={iconName} />
		<span class="text-white">{text}</span>
	</a>
</li>
