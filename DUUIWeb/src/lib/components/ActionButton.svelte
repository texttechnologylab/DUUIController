<script lang="ts">
	import type { IconDefinition } from '@fortawesome/free-solid-svg-icons'
	import Fa from 'svelte-fa'

	export let icon: IconDefinition
	export let text: string
	export let variant: string = 'variant-soft-surface'
	export let _class: string = ''
	export let leftToRight: boolean = true
</script>

<button on:click class="{_class} btn rounded-md {variant}">
	{#if leftToRight}
		<Fa {icon} />
		<span class="hidden md:block">{text}</span>
	{:else}
		<span class="hidden md:block">{text}</span>
		<Fa {icon} />
	{/if}
</button>
