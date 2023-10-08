<script lang="ts">
	import { componentStore } from '../../../routes/pipelines/new/store'

	let ignore200Error: boolean = false

	
</script>

<div class="grid grid-cols-1 card p-4 gap-4 pointer-events-auto">
	<label class="flex items-center space-x-2">
		<input class="checkbox" type="checkbox" bind:value={ignore200Error} />
		<span>Ignore Status 200 Error</span>
	</label>
</div>
