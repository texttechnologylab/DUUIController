<script lang="ts">
	import { goto } from '$app/navigation'
	import { page } from '$app/stores'
	import { ProgressRadial } from '@skeletonlabs/skeleton'
	import { onMount } from 'svelte'

	onMount(async () => {
		let code = $page.url.searchParams.get('code')
		if (code) {
			goto(`/account/user/connections?code=${$page.url.searchParams.get('code')}`, {
				invalidateAll: true
			})
		}
	})
</script>

<h1>Finishing connection...</h1>
<ProgressRadial value={undefined} />
