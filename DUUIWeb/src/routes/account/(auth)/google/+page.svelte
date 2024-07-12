<script lang="ts">
	import { goto } from '$app/navigation'
	import { faRefresh } from '@fortawesome/free-solid-svg-icons'
	import { onMount } from 'svelte'
	import Fa from 'svelte-fa'
	export let data
	const { success } = data

	onMount(() => {
		goto('/account?tab=1&success=' + success)
	})
</script>

<svelte:head>
	<title>Google Drive</title>
</svelte:head>

<div class="h-full w-full flex items-center justify-center p-4">
	<div class="space-y-8 section-wrapper p-8 flex flex-col items-center">
		<h1 class="h2">Finishing connection to Google Drive</h1>
		<hr class="hr" />
		<Fa icon={faRefresh} spin size="4x" />
		<p class="text-lg">Do not refresh this page</p>
	</div>
</div>
