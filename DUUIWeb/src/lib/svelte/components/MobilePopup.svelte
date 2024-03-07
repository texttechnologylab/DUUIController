<!-- 
	@component
	A component that shows a button (trigger) and when triggered a popup containing additional actions.
 -->
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
	<div class="popup-solid p-2 flex flex-col space-y-2">
		<slot />
	</div>
</div>

<button class="button-mobile" use:popup={dial}>
	<Fa icon={faEllipsis} />
	<span>More</span>
</button>
