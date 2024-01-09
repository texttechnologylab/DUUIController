<script lang="ts">
	import type { Placement } from '@floating-ui/dom'
	import { faChevronDown } from '@fortawesome/free-solid-svg-icons'
	import { popup, type PopupSettings } from '@skeletonlabs/skeleton'
	import Fa from 'svelte-fa'

	export let placement: Placement = 'bottom-start'
	export let background: string = 'hover:variant-filled-primary dark:hover:variant-soft-primary p-2'
	export let label: string
	export let offset: number = 8

	export let closeQuery: string = 'a[href]'

	const menu: PopupSettings = {
		event: 'click',
		target: label,
		placement: placement,
		closeQuery: closeQuery,
		middleware: {
			offset: offset
		}
	}
</script>

<button class="{background} text-sm transition-colors items-center btn" use:popup={menu}>
	<slot name="title" />
	<Fa icon={faChevronDown} />
</button>

<div
	class="z-50 shadow-lg rounded-md border-2 bg-white dark:bg-surface-600 border-surface-400/20 overflow-hidden"
	data-popup={label}
>
	<div class="grid text-left py-2">
		<slot name="content" />
	</div>
</div>
