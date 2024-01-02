<script lang="ts">
	import { faChevronDown, faChevronUp } from '@fortawesome/free-solid-svg-icons'
	import Fa from 'svelte-fa'
	import { fly } from 'svelte/transition'

	// export let visible: boolean = false
</script>

<div class="z-10 md:hidden fixed w-full left-0 bottom-0 flex flex-col gap-4 justify-center">
	<!-- {#if !visible}
		<button
			out:fly={{ delay: 200, y: 100 }}
			class="fixed end-4 bottom-4 variant-filled-primary dark:variant-soft-primary rounded-full btn-icon aspect-square mx-auto {visible
				? 'hidden'
				: 'inline-flex'}"
			on:click={() => (visible = !visible)}
		>
			<Fa icon={faChevronUp} />
		</button>
	{/if} -->
	<!-- {#if visible} -->
	<div
		in:fly={{ y: 100 }}
		out:fly={{ y: 100 }}
		class="flex justify-center gap-4 card p-2 rounded-none items-center border-t-[1px] bg-white dark:bg-surface-600 shadow-lg border-surface-400/20"
	>
		<div class="space-x-2 p-2">
			<slot name="content" />
			<!-- <button
					class="variant-filled-primary dark:variant-soft-primary rounded-full btn-icon aspect-square mx-auto"
					on:click={() => (visible = !visible)}
				>
					<Fa icon={faChevronDown} />
				</button> -->
		</div>
	</div>
	<!-- {/if} -->
</div>
