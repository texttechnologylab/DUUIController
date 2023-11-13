<script lang="ts">
	import type { IconDefinition } from '@fortawesome/free-solid-svg-icons'
	import Fa from 'svelte-fa'

	export let icon: IconDefinition
	export let text: string
	export let variant: string = 'dark:variant-soft-primary variant-filled-primary'
	export let rounded: string = 'rounded-none'
	export let _class: string = ''
	export let leftToRight: boolean = true
	export let tabindex: number = 0
	export let disabled: boolean = false
</script>

<button {tabindex} on:click class="btn {rounded} {variant} {_class}" {disabled}>
	{#if leftToRight}
		<Fa {icon} />
		<span>{text}</span>
	{:else}
		<span>{text}</span>
		<Fa {icon} />
	{/if}
</button>
