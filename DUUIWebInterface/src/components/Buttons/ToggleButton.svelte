<script lang="ts">
	import { Icon } from 'flowbite-svelte-icons';
	import { createEventDispatcher } from 'svelte';

	let disptach = createEventDispatcher();

	function onExpand() {
		disptach('expand');
		isExpanded = !isExpanded;
	}

	function onCollapse() {
		disptach('collapse');
		isExpanded = !isExpanded;
	}

	export let isExpanded: boolean = false;
	export let horizontal: boolean = false;

	let iconName: string;

	$: iconName = horizontal ? 'angle-left-solid' : 'angle-up-solid';
</script>

<button
	class="hover:bg-slate-800 hover:text-white cursor-pointer transition duration-300 p-3 flex items-center justify-center"
	on:click={() => (isExpanded ? onCollapse() : onExpand())}
>
	<Icon
		name={iconName}
		class="w-6 h-6 transition-transform duration-500 ease-out  {isExpanded ? '' : '-rotate-180'}"
	/>
</button>
