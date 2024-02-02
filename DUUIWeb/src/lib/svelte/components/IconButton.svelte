<script lang="ts">
	import type { IconDefinition } from '@fortawesome/free-solid-svg-icons'
	import Fa from 'svelte-fa'

	export let icon: IconDefinition
	export let variant: string = 'dark:variant-soft-primary variant-filled-primary'
	export let _class: string = ''
	export let rounded: string = 'rounded-full'
	export let size: string = ''
	export let disabled: boolean = false
</script>

<button
	on:click
	class="{_class} btn-icon {rounded} {variant} {disabled ? 'opacity-40 pointer-events-none' : ''}"
	{disabled}
>
	<Fa {size} {icon} />
</button>
