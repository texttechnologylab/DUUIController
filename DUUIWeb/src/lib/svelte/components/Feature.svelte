<script lang="ts">
	import type { IconDefinition } from '@fortawesome/free-solid-svg-icons'
	import Fa from 'svelte-fa'

	export let title: string
	export let content: string
	export let icon: IconDefinition
	export let classes: string = ''
</script>

<div
	class="border-color border-2
	p-4 py-12 space-y-4 relative bg-fancy
	text-center {classes}
	flex flex-col justify-start md:items-center md:border-l-2 md:pl-8 self-stretch
	"
>
	<div
		class="dimmed absolute rounded-full aspect-square p-4 bg-surface-50-900-token
		border-color border-2 left-1/2 -translate-x-1/2 top-0 -translate-y-1/2"
	>
		<Fa size="4x" {icon} class=" md:max-h-10 md:mb-4 hidden md:block" />
		<Fa size="2x" {icon} class=" md:max-h-10 md:mb-4 md:hidden block" />
	</div>
	<h3 class="text-2xl font-bold">{title}</h3>
	<p class="max-w-[45ch] dimmed">{content}</p>
	<slot />
</div>
