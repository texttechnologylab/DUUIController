<script lang="ts">
	import type { DUUIPipelineComponent } from '$lib/data'
	import { faThreads } from '@fortawesome/free-brands-svg-icons'
	import { faNetworkWired } from '@fortawesome/free-solid-svg-icons'
	import Fa from 'svelte-fa'

	export let component: DUUIPipelineComponent;
	
	let useGPU: boolean = false
	let dockerImageFetching: boolean = false
	
</script>

<div class="grid grid-cols-1 card p-4 gap-4 pointer-events-auto">
	<label class="flex items-center space-x-2">
		<input class="checkbox" type="checkbox" bind:value={useGPU} />
		<span>Utilize GPU</span>
	</label>
	<label class="flex items-center space-x-2">
		<input class="checkbox checked:variant-filled-primary" type="checkbox" bind:value={dockerImageFetching} />
		<span>Fetch Image if not available</span>
	</label>
	<!-- <label class="label">
		<span>Use GPU (Cuda)</span>
		<input class="input" type="number" placeholder="useGPU" bind:value={useGPU} />
	</label>
	<Fa icon={faNetworkWired} /> -->
</div>
