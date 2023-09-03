<script lang="ts">
	import { createEventDispatcher } from 'svelte';
	import Button from './Buttons/Button.svelte';
	const dispatcher = createEventDispatcher();

	export let dialog: HTMLDialogElement;

	let accept = () => {
		dispatcher('accept');
		dialog.close();
	};
	let cancel = () => {
		dispatcher('cancel');
		dialog.close();
	};
</script>

<dialog class="p-8 bg-white shadow-md" bind:this={dialog} on:close>
	<div class="flex flex-col gap-8">
		<slot />

		<div class="grid grid-cols-2 gap-4 items-center justify-center mt-8">
			<button
				class="bg-green-500 hover:bg-green-600 text-white font-bold pointer p-3 rounded-md
			transition-colors duration-300 ease-out"
				on:click={accept}
			>
				Accept
			</button>
			<button
				class="bg-red-500 hover:bg-red-600 text-white font-bold pointer p-3 rounded-md
			transition-colors duration-300 ease-out"
				on:click={cancel}
			>
				Cancel
			</button>
		</div>
	</div>
</dialog>
