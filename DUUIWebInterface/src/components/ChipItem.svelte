<script lang="ts">
	import { pipelineFilterStore } from '$lib/store';

	const onStateChanged = () => {
		isChecked = !isChecked;
		if (isChecked) {
			$pipelineFilterStore = [...$pipelineFilterStore, text];
		} else {
			$pipelineFilterStore = $pipelineFilterStore.filter((item) => item !== text);
		}
	};

	export let text: string;
	export let isChecked: boolean = false;
</script>

<!-- svelte-ignore a11y-role-supports-aria-props -->
<button
	on:click={onStateChanged}
	aria-selected={isChecked}
	class="rounded-md shadow-md p-3 aria-selected:bg-blue-500 aria-selected:text-white"
>
	<p>{text}</p>
</button>
