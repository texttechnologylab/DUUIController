<script lang="ts">
	import type { DUUIProcess } from '$lib/duui/process'
	import { faAngleDoubleRight, faClipboardList, faClose } from '@fortawesome/free-solid-svg-icons'
	import { clipboard, getDrawerStore } from '@skeletonlabs/skeleton'
	import Fa from 'svelte-fa'

	const drawerStore = getDrawerStore()
	export let process: DUUIProcess = $drawerStore.meta['process']

	const json = JSON.stringify(
		{
			pipeline_id: process.pipeline_id,
			input: process.input,
			output: process.output,
			settings: process.settings
		},
		null,
		2
	)
</script>

<div class="p-4 space-y-4 gradient bg-repeat h-screen">
	<div class="flex items-stretch gap-4 justify-between">
		<button class="button-neutral" on:click={drawerStore.close}>
			<Fa icon={faAngleDoubleRight} />
		</button>
		<button class="button-neutral" use:clipboard={json}>
			<Fa icon={faClipboardList} />
			<span>Copy</span>
		</button>
	</div>
	<pre>{json}</pre>
</div>
