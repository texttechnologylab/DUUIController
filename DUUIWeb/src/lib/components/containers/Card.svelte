<script lang="ts">
	import type { IconDefinition } from "@fortawesome/free-solid-svg-icons"
	import Fa from "svelte-fa"

	export let title: string
	export let content: string
    export let icon: IconDefinition;
    
</script>

<div class="card variant-glass-surface p-4 shadow-lg md:p-8 space-y-4 text-center">
	<span class="btn-icon btn-icon-xl variant-glass-primary">
		<Fa {icon} />
	</span>
	<h3 class="h3">{title}</h3>
	<p>{content}</p>
	<slot />
</div>
