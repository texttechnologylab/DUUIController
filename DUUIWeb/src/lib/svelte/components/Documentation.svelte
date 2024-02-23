<script lang="ts">
	import { page } from '$app/stores'
	import { faAngleRight } from '@fortawesome/free-solid-svg-icons'
	import Fa from 'svelte-fa'
</script>

{#if $page.url.pathname.includes('/documentation')}
	<aside
		class="border-color shadow-md border-r overflow-x-hidden overflow-y-auto h-full p-4 text-left space-y-8 hidden md:block w-auto text-lg"
	>
		<div class="space-y-4 px-2">
			<p class="h3 font-bold p-2">Documentation</p>
			<div class="flex flex-col gap-2">
				<a href="/documentation#introduction" class="flex items-center gap-4 p-2 animate-text">
					{#if $page.url.href.endsWith('introduction')}
						<Fa icon={faAngleRight} />
					{/if}
					Introduction
				</a>

				<a href="/documentation#pipeline" class="flex items-center gap-4 p-2 animate-text">
					{#if $page.url.href.endsWith('pipeline')}
						<Fa icon={faAngleRight} />
					{/if}
					Pipeline
				</a>

				<a href="/documentation#component" class="flex items-center gap-4 p-2 animate-text">
					{#if $page.url.href.endsWith('component')}
						<Fa icon={faAngleRight} />
					{/if}
					Component
				</a>

				<a href="/documentation#process" class="flex items-center gap-4 p-2 animate-text">
					{#if $page.url.href.endsWith('process')}
						<Fa icon={faAngleRight} />
					{/if}
					Process
				</a>

				<a class="flex items-center gap-4 p-2 animate-text" href="/documentation#document">
					{#if $page.url.href.endsWith('document')}
						<Fa icon={faAngleRight} />
					{/if}
					Document
				</a>
			</div>
		</div>

		<hr class="hr !w-full" />

		<div class="space-y-4 px-2">
			<p class="h3 font-bold p-2">API Reference</p>
			<div class="flex flex-col gap-2">
				<a href="/documentation/api#rest" class="flex items-center gap-4 p-2 animate-text">
					{#if $page.url.href.endsWith('rest')}
						<Fa icon={faAngleRight} />
					{/if}
					REST
				</a>

				<a class="flex items-center gap-4 p-2 animate-text" href="/documentation/api#java">
					{#if $page.url.href.endsWith('java')}
						<Fa icon={faAngleRight} />
					{/if}
					Java
				</a>
				<a class="flex items-center gap-4 p-2 animate-text" href="/documentation/api#python">
					{#if $page.url.href.endsWith('python')}
						<Fa icon={faAngleRight} />
					{/if}
					Python
				</a>
			</div>
		</div>
	</aside>
{/if}
