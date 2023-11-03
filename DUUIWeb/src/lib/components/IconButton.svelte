<script lang="ts">
	import type { IconDefinition } from '@fortawesome/free-solid-svg-icons'
	import Fa from 'svelte-fa'

	export let icon: IconDefinition
	export let variant: string = 'variant-soft-surface'
	export let _class: string = ''
	export let rounded: string = 'rounded-full'
	export let size: string = ''
</script>

<button on:click class="{_class} btn-icon {rounded} {variant}"><Fa {size} {icon} /> </button>
