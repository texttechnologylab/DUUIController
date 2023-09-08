<script lang="ts">
	import { faPlus } from '@fortawesome/free-solid-svg-icons'
	import Fa from 'svelte-fa'
</script>

<div class="p-8 flex flex-col gap-4 h-full items-center">
	<h2 class="h2">Could not load pipelines</h2>
	<a href="pipelines/new" class="btn variant-filled-primary">
		<span>Create new</span>
		<Fa icon={faPlus} />
	</a>
</div>
