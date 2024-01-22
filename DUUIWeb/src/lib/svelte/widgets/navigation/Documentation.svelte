<script lang="ts">
	import { page } from '$app/stores'
	import { faAngleRight } from '@fortawesome/free-solid-svg-icons'
	import Fa from 'svelte-fa'
</script>


{#if $page.url.pathname.includes('/documentation')}
	<aside
		class="overflow-x-hidden overflow-y-auto h-full p-8 text-left space-y-8 hidden md:block w-[400px] text-lg"
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

				<a class="flex items-center gap-4 p-2 animate-text" href="/documentation#composer">
					{#if $page.url.href.endsWith('composer')}
						<Fa icon={faAngleRight} />
					{/if}
					Composer
				</a>
				<a class="flex items-center gap-4 p-2 animate-text" href="/documentation#driver">
					{#if $page.url.href.endsWith('driver')}
						<Fa icon={faAngleRight} />
					{/if}
					Driver
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

		<!-- <div class="space-y-4 p-4">
			<p class="font-bold h3">Framework</p>
			<div class="flex flex-col text-left space-y-2">
				<a
					on:click={() => (selected = '#introduction')}
					href="/documentation#introduction"
					class="btn block text-left rounded-none
                {selected === '#introduction'
						? 'variant-filled-primary'
						: 'bg-primary-hover-token'}">Introduction</a
				>
				<a
					on:click={() => (selected = '#composer')}
					href="/documentation#composer"
					class=" btn block text-left rounded-none
                {selected === '#composer' ? 'variant-filled-primary' : 'bg-primary-hover-token'}"
					>Composer</a
				>

				<a
					on:click={() => (selected = '#driver')}
					href="/documentation#driver"
					class="btn block text-left rounded-none
                {selected === '#driver' ? 'variant-filled-primary' : 'bg-primary-hover-token'}"
					>Driver</a
				>

				<a
					on:click={() => (selected = '#component')}
					href="/documentation#component"
					class="btn block text-left rounded-none
                {selected === '#component' ? 'variant-filled-primary' : 'bg-primary-hover-token'}"
					>Component</a
				>
				<a
					on:click={() => (selected = '#document')}
					href="/documentation#document"
					class="btn block text-left rounded-none
                {selected === '#document' ? 'variant-filled-primary' : 'bg-primary-hover-token'}"
					>Document</a
				>
				<a
					on:click={() => (selected = '#process')}
					href="/documentation#process"
					class="btn block text-left rounded-none
                {selected === '#process' ? 'variant-filled-primary' : 'bg-primary-hover-token'}"
					>Process</a
				>
			</div>
		</div>
		<HLine thickness={2} width="w-3/4 mx-auto" />
		<div class="space-y-4 p-4">
			<p class="font-bold h3">API</p>
			<div class="flex flex-col text-left">
				<a
					on:click={() => (selected = '/api#pipeline')}
					href="/documentation/api#pipeline"
					class="btn block text-left rounded-none
                {selected === '/api#pipeline'
						? 'variant-filled-primary'
						: 'bg-primary-hover-token'}">Pipeline</a
				>

				<a
					on:click={() => (selected = '/api#component')}
					href="/documentation/api#component"
					class="btn block text-left rounded-none
                {selected === '/api#component'
						? 'variant-filled-primary'
						: 'bg-primary-hover-token'}">Component</a
				>

				<a
					on:click={() => (selected = '/api#process')}
					href="/documentation/api#process"
					class=" btn block text-left rounded-none
                {selected === '/api#process' ? 'variant-filled-primary' : 'bg-primary-hover-token'}"
					>Process</a
				>
				<a
					on:click={() => (selected = '/api#document')}
					href="/documentation/api#document"
					class=" btn block text-left rounded-none
                {selected === '/api#document'
						? 'variant-filled-primary'
						: 'bg-primary-hover-token'}">Document</a
				>
				<a
					on:click={() => (selected = '/api#event')}
					href="/documentation/api#event"
					class=" btn block text-left rounded-none
                {selected === '/api#event' ? 'variant-filled-primary' : 'bg-primary-hover-token'}"
					>Event</a
				>
			</div>
		</div> -->
	</aside>
{/if}
