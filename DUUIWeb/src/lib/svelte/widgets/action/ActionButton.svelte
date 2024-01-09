<script lang="ts">
	import type { IconDefinition } from '@fortawesome/free-solid-svg-icons'
	import Fa from 'svelte-fa'

	export let icon: IconDefinition | null = null
	export let text: string
	export let variant: string = 'dark:variant-soft-primary variant-filled-primary'
	export let rounded: string = ''
	export let _class: string = ''
	export let leftToRight: boolean = true
	export let tabindex: number = 0
	export let disabled: boolean = false
	export let visible: boolean = true
	export let small: boolean = false
</script>

<button
	data-sveltekit-preload-data="hover"
	{tabindex}
	on:click
	class="btn {small ? 'btn-sm' : ''} {visible
		? 'visible'
		: 'invisible'} {rounded} {variant} {_class}"
	{disabled}
>
	{#if leftToRight}
		{#if icon}
			<Fa {icon} />
		{/if}
		<span>{text}</span>
	{:else}
		<span>{text}</span>
		{#if icon}
			<Fa {icon} />
		{/if}
	{/if}
</button>
