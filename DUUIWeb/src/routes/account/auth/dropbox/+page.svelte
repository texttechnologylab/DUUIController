<script lang="ts">
	import { goto } from '$app/navigation'
	import { page } from '$app/stores'
	import { faExclamationTriangle } from '@fortawesome/free-solid-svg-icons'
	import { ProgressRadial } from '@skeletonlabs/skeleton'
	import { onMount } from 'svelte'
	import Fa from 'svelte-fa'

	let error: string

	onMount(async () => {
		let code = $page.url.searchParams.get('code')
		if (code) {
			goto(`/account/user/connections?code=${$page.url.searchParams.get('code')}`, {
				invalidateAll: true
			})
		} else {
			error = 'Code not found'
		}
	})
</script>

<div class="space-y-32">
	<div class="space-y-8">
		{#if error}
			<div class="flex items-center justify-between variant-filled-error p-4">
				<p>{error}</p>
				<Fa icon={faExclamationTriangle} size="lg" />
			</div>
		{/if}
		<h1 class="h2">Finishing connection</h1>
	</div>
	<ProgressRadial
		class="mx-auto"
		value={undefined}
		stroke={100}
		meter="stroke-primary-500"
		track="stroke-primary-500/30"
	/>
</div>
