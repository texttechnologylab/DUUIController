<script>
	// @ts-nocheck

	import { flip } from 'svelte/animate';
	import { fly } from 'svelte/transition';
	import { notifications } from '$lib/notifications';
	import { Icon } from 'flowbite-svelte-icons';
	import { sineOut } from 'svelte/easing';
</script>

<div class="fixed right-0 top-0 gap-4 grid grid-flow-row p-8 z-50">
	{#each $notifications as notification (notification.id)}
		<div
			animate:flip={{ duration: 300, easing: sineOut }}
			class="p-4 flex flex-col gap-4 text-white rounded-md min-w-[16rem] bg-[#333]"
			in:fly={{ x: 30, easing: sineOut }}
			out:fly={{ y: 30, easing: sineOut }}
		>
			<div class="  flex items-center gap-4">
				<Icon name={notification.icon} />
				<p class="text-lg font-bold font-heading">{notification.title}</p>
			</div>
			<p class="leading-8">{notification.message}</p>
		</div>
	{/each}
</div>
