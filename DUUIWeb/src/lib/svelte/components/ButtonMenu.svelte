<script lang="ts">
	import { faEllipsis } from '@fortawesome/free-solid-svg-icons'
	import { popup, type PopupSettings } from '@skeletonlabs/skeleton'
	import Fa from 'svelte-fa'

	const dial: PopupSettings = {
		placement: 'top',
		target: 'popup',
		event: 'click',
		middleware: {
			offset: 4
		}
	}
</script>

<div data-popup="popup">
	<div class="flex flex-col bg-surface-50-900-token shadow-lg border border-color">
		<slot />
	</div>
</div>

<button class="button-mobile" use:popup={dial}>
	<Fa icon={faEllipsis} />
	<span>More</span>
</button>
