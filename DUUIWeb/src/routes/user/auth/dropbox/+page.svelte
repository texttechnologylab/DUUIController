<script lang="ts">
	import { Dropbox } from 'dropbox'
	import { DropboxAuth } from 'dropbox'

	import { browser } from '$app/environment'

	const redirectURI = 'http://localhost:5173/pipelines'
	let dbxAuth = new DropboxAuth({
		clientId: 'l2nw2ign2z8h9hg'
	})

	const doAuth = () => {
		dbxAuth
			.getAuthenticationUrl(redirectURI, undefined, 'code', 'offline', undefined, undefined, true)
			.then((url) => {
				if (browser) {
					window.sessionStorage.clear()
					window.sessionStorage.setItem('codeVerifier', dbxAuth.getCodeVerifier())
					window.location.href = url
				}
			})
			.catch((error) => console.error(error))
	}
</script>

<button class="btn variant-glass-primary" on:click={doAuth}> Authenticate </button>
