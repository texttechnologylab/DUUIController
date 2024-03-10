<!--
	@component
	A component representing a tooltip or menu that is visible when hovering the trigger.
	
	Uses Svelte slots for both the trigger and popup parts.
-->

<script lang="ts">
	export let arrow: boolean = true
	export let position: 'top' | 'bottom' | 'left' | 'right' = 'bottom'
	export let classes: string = ''
</script>

<div class="group relative {classes}">
	<slot name="trigger" />
	{#if position === 'bottom'}
		<div
			class="invisible group-hover:visible translate-y-4 transition-all origin-top opacity-0
                absolute left-1/2 -translate-x-1/2 top-full z-[9999]
                group-hover:translate-y-0 duration-300 group-hover:opacity-100"
		>
			<div class="py-4 relative">
				<slot name="popup" />
				{#if arrow}
					<div
						class="absolute rotate-45 top-2 w-2 h-2
                            left-1/2 translate-y-1/2
                            -translate-x-1/2 bg-surface-50-900-token border-l border-t border-color"
					/>
				{/if}
			</div>
		</div>
	{:else if position === 'top'}
		<div
			class="invisible group-hover:visible -translate-y-4 transition-all origin-bottom opacity-0
                absolute left-1/2 -translate-x-1/2 bottom-full z-[100]
                group-hover:translate-y-0 duration-300 group-hover:opacity-100"
		>
			<div class="py-4 relative">
				<slot name="popup" />

				{#if arrow}
					<div
						class="absolute rotate-45 bottom-2 w-2 h-2
                            left-1/2 -translate-y-1/2
                            -translate-x-1/2 bg-surface-50-900-token border-r border-b border-color"
					/>
				{/if}
			</div>
		</div>
	{:else if position === 'right'}
		<div
			class="invisible group-hover:visible -translate-x-8 transition-all origin-bottom opacity-0
                absolute right-2 top-1/2 -translate-y-1/2 z-[100]
                group-hover:translate-x-full duration-300 group-hover:opacity-100"
		>
			<div class="translate-x-4 relative">
				<slot name="popup" />

				{#if arrow}
					<div
						class="absolute rotate-45 top-1/2 w-2 h-2
                            left-0 -translate-x-1/2
                            -translate-y-1/2 bg-surface-50-900-token border-b border-l border-color"
					/>
				{/if}
			</div>
		</div>
	{:else if position === 'left'}
		<div
			class="invisible group-hover:visible -translate-x-8 transition-all origin-bottom opacity-0
                absolute left-2 top-1/2 -translate-y-1/2 z-[100]
                group-hover:-translate-x-full duration-300 group-hover:opacity-100"
		>
			<div class="-translate-x-4 relative">
				<slot name="popup" />

				{#if arrow}
					<div
						class="absolute rotate-45 top-1/2 w-2 h-2
                            right-0 translate-x-1/2
                            -translate-y-1/2 bg-surface-50-900-token border-t border-r border-color"
					/>
				{/if}
			</div>
		</div>
	{/if}
</div>
