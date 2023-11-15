<script lang="ts">
	import type { IconDefinition } from '@fortawesome/free-solid-svg-icons'
	import Fa from 'svelte-fa'

	export let icon: IconDefinition
	export let href: string
	export let text: string = ''
	export let variant: string = 'variant-ringed-primary'
	export let _class: string = ''
	export let leftToRight: boolean = true
	export let rounded: string = 'rounded-none'
	export let target: string = ''
	export let rel: string = ''
	export let size: string = 'md'
</script>

<a {href} {rel} {target} class="btn {rounded} {variant} {_class}">
	{#if leftToRight}
		<Fa class="w-4" {icon} {size} />
		{#if text !== ''}
			<span>{text}</span>
		{/if}
	{:else}
		{#if text !== ''}
			<span>{text}</span>
		{/if}
		<Fa class="w-4" {icon} {size} />
	{/if}
</a>
