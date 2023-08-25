<script lang="ts">
	import { Icon } from 'flowbite-svelte-icons';
	import { createEventDispatcher } from 'svelte';

	const dispatch = createEventDispatcher();
	export let text: string;
	export let isCancel: boolean = false;
	export let iconName: string;

	function clickEvent() {
		dispatch('click', 'generate');
	}
</script>

{#if !isCancel}
	<a
		class="button font-bold border-none cursor-pointer no-underline
			  text-white rounded-md flex gap-4 items-center py-3 px-4 transition-colors
			bg-green-500 hover:bg-green-600"
		href="#"
		on:click
	>
		<Icon class="text-white w-5 h-5" name={iconName} />
		<span>{text}</span>
	</a>
{:else}
	<a
		class="button font-bold border-none cursor-pointer no-underline
		 text-white rounded-md flex gap-4 items-center justify-start py-3 px-4 transition-colors
			bg-red-500 hover:bg-red-600"
		href="#"
		on:click
	>
		<Icon class="text-white w-5 h-5" name={iconName} />
		<span>{text}</span>
	</a>
{/if}
