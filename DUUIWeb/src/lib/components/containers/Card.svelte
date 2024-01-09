<script lang="ts">
	import type { IconDefinition } from '@fortawesome/free-solid-svg-icons'
	import Fa from 'svelte-fa'

	export let title: string
	export let content: string
	export let icon: IconDefinition
	export let classes: string = ''
</script>

<div
	class=" border-surface-400/20
	p-4 py-8 space-y-4
	text-center md:text-left {classes}
	flex flex-col justify-center md:items-start"
>	

	<Fa size="4x" {icon} class="text-primary-500 md:max-h-10 md:mb-4 hidden md:block" />
	<Fa size="2x" {icon} class="text-primary-500 md:max-h-10 md:mb-4 md:hidden block" />
	<h3 class="text-2xl font-bold">{title}</h3>
	<p class="text-surface-400 dark:text-surface-200">{content}</p>
	<slot />
</div>
