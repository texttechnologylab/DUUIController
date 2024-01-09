<script lang="ts">
	import { fly } from 'svelte/transition'
</script>

<div
	class="overflow-hidden z-10 md:hidden fixed w-full left-0 bottom-0 flex flex-col gap-4 justify-center p-4"
>
	<div
		in:fly={{ y: 100 }}
		out:fly={{ y: 100 }}
		class=" rounded-full flex justify-center gap-4 items-center shadow-lg overflow-hidden"
	>
		<div
			class="flex flex-auto [&>*]:flex-1 [&>*]:rounded-none [&>*:not(:last-child)]:border-r
			 [&>*]:border-r-surface-100/20 items-center max-h-12"
		>
			<slot name="content" />
		</div>
	</div>
</div>
