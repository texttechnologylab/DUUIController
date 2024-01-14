<script lang="ts">
	import { page } from '$app/stores'
	import SpeedDial from '$lib/svelte/widgets/navigation/BottomMenu.svelte'
	import { toTitleCase } from  '$lib/duui/utils/text'
	import { faUser, faLink, faLock } from '@fortawesome/free-solid-svg-icons'
	import Fa from 'svelte-fa'

	let section: string = $page.url.pathname.split('/').at(-1) || 'profile'
	$: section = $page.url.pathname.split('/').at(-1) || 'profile'
</script>

<svelte:head>
	<title>Account</title>
</svelte:head>

<SpeedDial>
	<svelte:fragment slot="content">
		<a href="/account/user/profile" class="btn variant-filled-primary p-4">
			<Fa icon={faUser} />
		</a>
		<a href="/account/user/connections" class="btn variant-filled-primary p-4">
			<Fa icon={faLink} />
		</a>
		<a href="/account/user/security" class="btn variant-filled-primary p-4">
			<Fa icon={faLock} />
		</a>
	</svelte:fragment>
</SpeedDial>

<div class="p-4 md:py-8 container max-w-7xl mx-auto">
	<div
		class="hidden sticky top-8 md:grid grid-cols-3
		 rounded-full overflow-hidden bg-surface-100 dark:variant-soft-surface z-[2]
		  shadow-lg text-xs md:text-base"
	>
		<a
			href="/account/user/profile"
			class="flex items-center gap-4 justify-center p-2
			 {section === 'profile' ? 'variant-filled-primary' : ''}"
		>
			<Fa icon={faUser} />
			<span>Profile</span>
		</a>

		<a
			href="/account/user/connections"
			class="flex items-center gap-4 justify-center p-2
			 {section === 'connections' ? 'variant-filled-primary' : ''}"
		>
			<Fa icon={faLink} />
			<span>Connections</span>
		</a>

		<a
			href="/account/user/security"
			class="flex items-center gap-4 justify-center p-2
			 {section === 'security' ? 'variant-filled-primary' : ''}"
		>
			<Fa icon={faLock} />
			<span>Security</span>
		</a>
	</div>

	<div class=" space-y-4 md:py-8 md:h-full">
		<h1 class="h2">{toTitleCase(section)}</h1>
		<hr class="bg-surface-400/20 h-[1px] !border-0 rounded" />
		<slot />
	</div>
</div>
