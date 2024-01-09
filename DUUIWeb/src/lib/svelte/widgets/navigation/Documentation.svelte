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
	class="h-screen sticky top-0 space-y-4 bg-surface-100 dark:bg-surface-700 md:w-modal-slim p-4"
>
	<!-- Introduction -->
	<div class="space-y-4 p-4">
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
	</div>
</aside>
