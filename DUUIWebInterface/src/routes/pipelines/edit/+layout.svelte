<script lang="ts">
	import { goto } from '$app/navigation';
	import { notifications } from '$lib/notifications';
	import { currentPipelineStore, pipelineStore } from '$lib/store';
	import Dialog from '../../../components/Dialog.svelte';
	import { deletePipeline } from '../../../requests/delete';

	let dialog: HTMLDialogElement;

	function onDelete() {
		$pipelineStore = $pipelineStore.filter(function (pipeline, i, arr) {
			if (pipeline.id !== $currentPipelineStore.id) return pipeline;
		});
		deletePipeline($currentPipelineStore.id);
		goto('/pipelines');
		notifications.success("Pipeline has been deleted.", $currentPipelineStore.id, 5000);
	}
</script>

<div class="min-h-screen m-16 flex gap-8 ">
	<aside class="bg-[#f5f5f5] shadow-md p-8">
		<button on:click={() => dialog.showModal()}> Delete Pipeline </button>
	</aside>
	<slot />
</div>

<Dialog bind:dialog on:accept={onDelete}>
	<p class="text-lg">Delete Component</p>
	<p class="text-md max-w-[40ch]">
		Are you sure you want to delete <strong>{$currentPipelineStore.name}</strong>? This cannot be
		undone.
	</p>
</Dialog>
