<script lang="ts">
	import type { IconDefinition } from '@fortawesome/free-solid-svg-icons'
	import Fa from 'svelte-fa'

	export let title: string
	export let content: string
	export let icon: IconDefinition
	export let classes: string = ""
</script>

<div class="variant-soft-surface p-4 shadow-lg space-y-4 text-left {classes} min-h-[12rem]">
	<div class="flex items-center justify-between">
		<h3 class="h3 font-bold">{title}</h3>
		<Fa size="2x" {icon} />
	</div>
	<p>{content}</p>
	<slot />
</div>
