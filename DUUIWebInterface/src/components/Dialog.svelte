<script lang="ts">
	import { createEventDispatcher } from 'svelte';
	import Button from './Button/Button.svelte';
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

<dialog class="p-8 bg-white border-2 border-slate-900" bind:this={dialog} on:close>
	<slot />
	<div class="py-4 grid grid-cols-2 gap-4 items-center justify-center">
		<Button text="Accept" iconName="check-solid" on:click={accept} />
		<Button text="Cancel" iconName="close-solid" isCancel={true} on:click={cancel} />
	</div>
</dialog>
