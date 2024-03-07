<!-- @component A component that represents a feature on the front page. -->
<script lang="ts">
	import type { IconDefinition } from '@fortawesome/free-solid-svg-icons'
	import Fa from 'svelte-fa'

	export let title: string
	export let content: string
	export let icon: IconDefinition
	export let classes: string = ''
</script>

<div
	class="
	p-4 md:pt-24 pb-4 space-y-4 relative bg-fancy first-of-type:pt-24
	text-center {classes}
	flex flex-col justify-center items-center self-stretch
	"
>
	<div class="aspect-square flex justify-center rounded-md py-4 items-center">
		<Fa size="2x" {icon} />
	</div>
	<h2 class="h3">{title}</h2>
	<p class="mx-auto max-w-[45ch]">{content}</p>
	<slot />
</div>
