<script lang="ts">
	import { faCheck, faClose, type IconDefinition } from '@fortawesome/free-solid-svg-icons'
	import Fa from 'svelte-fa'

	export let label: string
	export let name: string
	export let size:
		| 'xs'
		| 'sm'
		| ''
		| 'lg'
		| '1x'
		| '2x'
		| '3x'
		| '4x'
		| '5x'
		| '6x'
		| '7x'
		| '8x'
		| '9x'
		| '10x' = ''
	export let checked: boolean = false
	export let column: boolean = false

	export let iconChecked: IconDefinition = faCheck
	export let iconUnchecked: IconDefinition | null = null
</script>

<label class="flex {column ? 'flex-col-reverse' : ''} items-start gap-4 text-sm">
	<button on:click={() => (checked = !checked)}>
		<div
			class="rounded-md p-1 aspect-square border-[1px] bg-white dark:bg-surface-600 border-surface-400/20"
		>
			{#if iconUnchecked === null}
				<Fa
					icon={iconChecked}
					{size}
					class="aspect-square transition-transform {checked
						? 'scale-100'
						: 'scale-0'} duration-100"
				/>
			{:else}
				<Fa icon={checked ? iconChecked : iconUnchecked} {size} class="aspect-square" />
			{/if}
		</div>
	</button>
	<input type="checkbox" {name} bind:checked class="appearance-none hidden" />
	<span class="{column ? 'form-label' : ''} cursor-pointer">{label}</span>
</label>
