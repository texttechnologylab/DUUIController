<script lang="ts">
	import { faLightbulb } from '@fortawesome/free-solid-svg-icons'
	import Fa from 'svelte-fa'

	export let showIcon: boolean = true
</script>

<div class="flex gap-4 p-4 bordered-soft rounded-md variant-soft-primary items-center my-4">
	{#if showIcon}
		<Fa icon={faLightbulb} size="lg" />
	{/if}
	<div class={showIcon ? 'border-l pl-4 border-color' : ''}>
		<slot />
	</div>
</div>
