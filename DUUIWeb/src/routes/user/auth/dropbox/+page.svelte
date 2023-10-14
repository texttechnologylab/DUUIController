<script lang="ts">
	import { page } from '$app/stores'
	import { onMount } from 'svelte'


	let success: boolean = false

	onMount(async () => {
		let code = $page.url.searchParams.get('code')
		if (code) {
			const response = await fetch('/user/api/auth/dropbox/refresh', {
				method: 'POST',
				body: JSON.stringify({
					code: code
				})
			})
			success = response.ok
		}
	})
</script>

<button class="btn variant-filled-primary"> {success ? 'Success' : 'Failed'} </button>
