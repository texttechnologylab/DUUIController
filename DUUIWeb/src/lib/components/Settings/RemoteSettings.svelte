<script lang="ts">
	import type { DUUIPipelineComponent } from '$lib/data'
	import { currentPipelineStore } from '$lib/store'

	export let component: DUUIPipelineComponent
</script>

<div class="grid grid-cols-1 card p-4 gap-4 pointer-events-auto">
	<label class="flex items-center space-x-2">
		<input class="checkbox" type="checkbox" />
		<span>Ignore Status 200 Error</span>
	</label>

	<!-- <label class="label">
		<span>Use GPU (Cuda)</span>
		<input class="input" type="number" placeholder="useGPU" bind:value={useGPU} />
	</label>
	<Fa icon={faNetworkWired} /> -->
</div>
