<script lang="ts">
	import { page } from '$app/stores'
	import { toTitleCase } from '$lib/utils/text'
	import { faUser, faLink, faLock } from '@fortawesome/free-solid-svg-icons'
	import Fa from 'svelte-fa'

	let section: string = $page.url.pathname.split('/').at(-1) || 'profile'
	$: section = $page.url.pathname.split('/').at(-1) || 'profile'
</script>

<div class=" relative h-full container mx-auto grid items-start">
	<div class="md:flex md:py-4 gap-4">
		<aside
			class="h-full p-4 sticky top-0 md:top-4 flex flex-col gap-4 justify-start bg-white dark:bg-surface-700 md:w-modal-slim"
		>
			<a
				href="/account/user/profile"
				class="bg-primary-hover-token flex items-center gap-4 md:gap-16 justify-between p-3 border-l-8 border-primary-500/50 {section ===
				'profile'
					? 'bg-primary-500/10'
					: 'border-l-transparent'}"
				on:click={() => (section = 'profile')}
			>
				<span>Profile</span>
				<Fa icon={faUser} />
			</a>
			<a
				href="/account/user/connections"
				class="bg-primary-hover-token flex items-center gap-4 md:gap-16 justify-between p-3 border-l-8 border-primary-500/50 {section ===
				'connections'
					? 'bg-primary-500/10'
					: 'border-l-transparent'}"
				on:click={() => (section = 'connections')}
			>
				<span>Connections</span>
				<Fa icon={faLink} />
			</a>
			<a
				href="/account/user/security"
				class="bg-primary-hover-token flex items-center gap-4 md:gap-16 justify-between p-3 border-l-8 border-primary-500/50 {section ===
				'security'
					? 'bg-primary-500/10'
					: 'border-l-transparent'}"
				on:click={() => (section = 'security')}
			>
				<span>Security</span>
				<Fa icon={faLock} />
			</a>
		</aside>
		<div class="p-4 space-y-4 w-full">
			<h2 class="h2">{toTitleCase(section)}</h2>
			<hr class="bg-surface-400/20 h-[1px] !border-0 rounded" />
			<slot />
		</div>
	</div>
</div>
