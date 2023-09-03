<script lang="ts">
	import { page } from '$app/stores'
	import { faBars } from '@fortawesome/free-solid-svg-icons'
	import { getDrawerStore } from '@skeletonlabs/skeleton'
	import Fa from 'svelte-fa'

	$: classesActive = (href: string) => (href === $page.url.pathname ? '!bg-primary-500' : '')

	export let hidden: boolean = false
	export let withNavigation: boolean = false

	const drawerStore = getDrawerStore();
	
</script>

<aside
	class="{hidden
		? 'hidden'
		: 'flex'} lg:flex h-full flex-none overflow-x-hidden overflow-y-auto bg-surface-50-900-token lg:w-auto"
>
	<section class="p-4 pb-20 space-y-4 overflow-y-auto">
		{#if withNavigation}
			<div class="flex gap-4 justify-start items-center">
				<button class="btn-icon" on:click={() => drawerStore.close()}
					><Fa size="lg" icon={faBars} /></button
				>
				<p class="font-bold pl-4 text-2xl">Navigation</p>
			</div>
			<nav class="list-nav">
				<ul>
					<li>
						<a href="/" class={classesActive('/')} data-sveltekit-preload-data="hover"
							><span class="flex-auto">Home</span>
						</a>
					</li>
					<li>
						<a
							href="/pipelines"
							class={classesActive('/pipelines')}
							data-sveltekit-preload-data="hover"
							><span class="flex-auto">Pipelines</span>
						</a>
					</li>
					<li>
						<a href="/login" class={classesActive('/login')} data-sveltekit-preload-data="hover"
							><span class="flex-auto">Login</span>
						</a>
					</li>
				</ul>
			</nav>
			<hr class="!my-6 opacity-50" />
		{/if}

		<p class="font-bold pl-4 text-2xl">Docs</p>
		<nav class="list-nav">
			<ul>
				<li>
					<a href="/docs" class={classesActive('/docs')} data-sveltekit-preload-data="hover"
						><span class="flex-auto">Docker Unified UIMA Interface</span>
					</a>
				</li>
				<li>
					<a
						href="/docs/composer"
						class={classesActive('/docs/composer')}
						data-sveltekit-preload-data="hover"
						><span class="flex-auto">Composer</span>
					</a>
				</li>
				<li>
					<a
						href="/docs/drivers"
						class={classesActive('/docs/drivers')}
						data-sveltekit-preload-data="hover"
						><span class="flex-auto">Drivers</span>
					</a>
				</li>
				<li>
					<a
						href="/docs/components"
						class={classesActive('/docs/components')}
						data-sveltekit-preload-data="hover"
						><span class="flex-auto">Components</span>
					</a>
				</li>
				<li>
					<a
						href="/docs/communication"
						class={classesActive('/docs/communication')}
						data-sveltekit-preload-data="hover"
						><span class="flex-auto">Communication</span>
					</a>
				</li>
			</ul>
		</nav>
	</section>
</aside>
