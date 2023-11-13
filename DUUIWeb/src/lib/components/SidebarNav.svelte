<script lang="ts">
	import { page } from '$app/stores'
	import Anchor from '$lib/svelte/widgets/action/Anchor.svelte'
	import IconButton from '$lib/svelte/widgets/action/IconButton.svelte'
	import Icon from '$lib/assets/favicon.svg'
	import { faBars, faBookOpen, faGears, faHome, faUser } from '@fortawesome/free-solid-svg-icons'
	import { Accordion, AccordionItem, getDrawerStore } from '@skeletonlabs/skeleton'
	import Fa from 'svelte-fa'

	$: classesActive = (href: string) => (href === $page.url.pathname ? 'variant-filled-primary' : '')

	export let hidden: boolean = false
	export let withNavigation: boolean = false

	const drawerStore = getDrawerStore()
</script>

<aside
	class="{hidden
		? 'hidden'
		: 'flex'} h-full overflow-x-hidden overflow-y-auto bg-surface-50-900-token"
>
	<section class="p-4 space-y-4 overflow-y-auto">
		{#if withNavigation}
			<div class="flex items-center justify-between gap-4 mb-4">
				<IconButton variant="" icon={faBars} size="lg" on:click={() => drawerStore.close()} />
				<img src={Icon} class="block w-12 variant-surface" alt="" />
			</div>
			<nav class="list-nav">
				<ul>
					<li>
						<Anchor
							href="/"
							icon={faHome}
							text="Home"
							_class="justify-between"
							variant={classesActive('/')}
						/>
					</li>
					<li>
						<Anchor
							href="/pipelines"
							icon={faGears}
							text="Pipelines"
							_class="justify-between"
							variant={classesActive('/pipelines')}
						/>
					</li>
					<li>
						<Anchor
							href="/user/login"
							icon={faUser}
							text="Account"
							_class="justify-between"
							variant={classesActive('/user/login')}
						/>
					</li>
				</ul>
			</nav>
			<hr class="!my-6 opacity-50" />
		{/if}

		<Accordion>
			<AccordionItem>
				<svelte:fragment slot="lead"><Fa icon={faBookOpen} /></svelte:fragment>
				<svelte:fragment slot="summary">Documentation</svelte:fragment>
				<svelte:fragment slot="content">
					<nav class="list-nav">
						<ul>
							<li>
								<a
									href="/documentation"
									class={classesActive('/documentation')}
									data-sveltekit-preload-data="hover"
									><span class="flex-auto">Quick Start</span>
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
				</svelte:fragment>
			</AccordionItem>
		</Accordion>
	</section>
</aside>
