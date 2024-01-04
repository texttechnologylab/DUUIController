<script lang="ts">
	import { page } from '$app/stores'
</script>

<div class="card p-4 container mx-auto">
	<p>{$page.error?.message}</p>
</div>
