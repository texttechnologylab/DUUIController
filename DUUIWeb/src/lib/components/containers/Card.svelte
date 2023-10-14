<script lang="ts">
	import type { IconDefinition } from '@fortawesome/free-solid-svg-icons'
	import Fa from 'svelte-fa'

	export let title: string
	export let content: string
	export let icon: IconDefinition
</script>

<div class="card rounded-md p-4 shadow-lg space-y-4 text-center flex flex-col items-center hover:-translate-y-4 transition-transform">
	<div class="variant-glass-primary p-4 aspect-square rounded-full">
		<Fa size="lg" {icon} />
	</div>
	<h3 class="h3">{title}</h3>
	<p>{content}</p>
	<slot />
</div>
