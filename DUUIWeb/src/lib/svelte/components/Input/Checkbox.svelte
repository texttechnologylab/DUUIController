<script lang="ts">
	import { faCheck, type IconDefinition } from '@fortawesome/free-solid-svg-icons'
	import Fa from 'svelte-fa'

	export let label: string
	export let name: string = ''
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

<label class="flex {column ? 'flex-col-reverse' : ''} items-center gap-4 text-sm">
	<button on:click={() => (checked = !checked)}>
		<div class="input-wrapper aspect-square !p-1 {checked ? '!variant-filled-primary' : ''}">
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
	<input tabindex="-1" type="checkbox" {name} bind:checked class="sr-only" />
	<span class="{column ? 'form-label' : ''} cursor-pointer">{label}</span>
</label>
