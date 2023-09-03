<script lang="ts">
	import { createEventDispatcher } from 'svelte';

	const dispatcher = createEventDispatcher();

	const onStateChanged = () => {
		isChecked = !isChecked;
		dispatcher('change', {
			checked: isChecked
		});
	};

	export let text: string;
	export let isChecked: boolean = false;
</script>

<button on:click={onStateChanged}
    class:active={isChecked}
    class="rounded-md shadow-md p-4 active:bg-blue-500 active:tex-white">
	<p>{text}</p>
</button>
