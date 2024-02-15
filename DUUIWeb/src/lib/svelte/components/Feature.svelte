<script lang="ts">
	import type { IconDefinition } from '@fortawesome/free-solid-svg-icons'
	import Fa from 'svelte-fa'

	export let title: string
	export let content: string
	export let icon: IconDefinition
	export let classes: string = ''
</script>

<div
	class="border-color border
	p-4 py-8 space-y-4 relative bg-fancy
	text-start {classes} shadow-md
	flex flex-col justify-start items-start border-l-8 !border-l-primary-500 rounded-md bg-surface-200/10 md:pl-8 self-stretch
	"
>
	<div class="aspect-square flex justify-start rounded-md py-4 dimmed items-center">
		<Fa size="2x" {icon} />
	</div>
	<h3 class="text-2xl font-bold">{title}</h3>
	<p class="max-w-[45ch] dimmed">{content}</p>
	<slot />
</div>
