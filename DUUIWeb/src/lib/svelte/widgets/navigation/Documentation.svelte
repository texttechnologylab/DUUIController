<script lang="ts">
	import HLine from './HLine.svelte'
	import { page } from '$app/stores'

	const url = $page.url.href
	const regex = /documentation(.*)$/
	const match = url.match(regex)

	let selected = ''
	$: {
		if (match && match.length > 1) {
			selected = match[1]
		}
	}
</script>

<aside
	class="h-full !sticky top-0 flex flex-col gap-4 justify-start bg-white dark:bg-surface-700 md:w-modal-slim p-4"
>
	<!-- Introduction -->
	<div class="space-y-4 p-4">
		<p class="font-bold h3">Framework</p>
		<div class="flex flex-col text-left space-y-2">
			<a
				on:click={() => (selected = '#introduction')}
				href="/documentation#introduction"
				class="btn block text-left rounded-none
                {selected === '#introduction' ? 'variant-filled-primary' : 'bg-primary-hover-token'}"
				>Introduction</a
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
				on:click={() => (selected = '/api#web')}
				href="/documentation/api#web"
				class="btn block text-left rounded-none
                {selected === '/api#web' ? 'variant-filled-primary' : 'bg-primary-hover-token'}"
				>Web</a
			>

			<a
				on:click={() => (selected = '/api#java')}
				href="/documentation/api#java"
				class="btn block text-left rounded-none
                {selected === '/api#java' ? 'variant-filled-primary' : 'bg-primary-hover-token'}"
				>Java</a
			>

			<a
				on:click={() => (selected = '/api#python')}
				href="/documentation/api#python"
				class=" btn block text-left rounded-none
                {selected === '/api#python' ? 'variant-filled-primary' : 'bg-primary-hover-token'}"
				>Python</a
			>
		</div>
	</div>
</aside>
