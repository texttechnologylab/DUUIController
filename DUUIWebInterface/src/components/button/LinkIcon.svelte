<script lang="ts">
	import { Icon } from 'flowbite-svelte-icons';

	export let href: string = '#';
	export let iconName: string;

	$: target = href !== '#' ? '_blank' : '';
</script>

<a {href} class="hover:text-blue-400 transition-colors duration-300 ease-out" {target}>
	<Icon class="w-8 h-8" name={iconName} />
</a>
