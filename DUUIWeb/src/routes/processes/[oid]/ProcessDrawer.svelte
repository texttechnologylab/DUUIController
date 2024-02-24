<script lang="ts">
	import type { DUUIProcess } from '$lib/duui/process'
	import { successToast } from '$lib/duui/utils/ui'
	import { faClipboardList, faClose } from '@fortawesome/free-solid-svg-icons'
	import { clipboard, getDrawerStore, getToastStore } from '@skeletonlabs/skeleton'
	import Fa from 'svelte-fa'

	const drawerStore = getDrawerStore()
	const toastStore = getToastStore()
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

<div class=" space-y-4 h-full bg-surface-100-800-token">
	<div
		class="flex items-stretch gap-4 justify-between border-b border-color bg-surface-50-900-token"
	>
		<button
			class="button-menu border-r border-color"
			use:clipboard={json}
			on:click={() => {
				toastStore.trigger(successToast('Copied!'))
			}}
		>
			<Fa icon={faClipboardList} />
			<span>Copy</span>
		</button>
		<p class="text-lg self-center">Settings</p>
		<button class="button-menu border-l border-color" on:click={drawerStore.close}>
			<Fa icon={faClose} />
			<span>Close</span>
		</button>
	</div>
	<pre class="p-4 break-words">{json}</pre>
</div>

<style>
	pre {
		white-space: pre-wrap; /* Since CSS 2.1 */
		white-space: -moz-pre-wrap; /* Mozilla, since 1999 */
		white-space: -pre-wrap; /* Opera 4-6 */
		white-space: -o-pre-wrap; /* Opera 7 */
		word-wrap: break-word; /* Internet Explorer 5.5+ */
	}
</style>
