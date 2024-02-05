<script lang="ts">
	import { goto } from '$app/navigation'
	import { ProgressBar } from '@skeletonlabs/skeleton'
	import { onMount } from 'svelte'
	export let data
	const { success } = data

	onMount(() => {
		goto('/account?success=' + success)
	})
</script>

<div class="space-y-8 text-center">
	<h1 class="h1">Finishing connection to Dropbox</h1>
	<ProgressBar value={undefined} rounded="rounded-full" />
	<p class="text-lg">Do not refresh this page</p>
</div>
