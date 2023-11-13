<script lang="ts">
	import type { Placement } from '@floating-ui/dom'
	import { faChevronDown } from '@fortawesome/free-solid-svg-icons'
	import { popup, type PopupSettings } from '@skeletonlabs/skeleton'
	import Fa from 'svelte-fa'

	export let placement: Placement = 'bottom-start'
	export let background: string = ''
	export let name: string
	export let offset: number = 12

	export let closeQuery: string = 'a[href]'

	const menu: PopupSettings = {
		event: 'click',
		target: name,
		placement: placement,
		closeQuery: closeQuery,
		middleware: {
			offset: offset
		}
	}
</script>

<button
	class="{background} cursor-pointer inline-flex text-sm transition-colors items-center gap-2"
	use:popup={menu}
>
	<slot name="title" />
	<Fa icon={faChevronDown} />
</button>

<div data-popup={name}>
	<slot name="content" />
</div>
