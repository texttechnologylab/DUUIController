<script lang="ts">
	import type { IconDefinition } from '@fortawesome/free-solid-svg-icons'
	import Fa from 'svelte-fa'

	export let value: string = ''
	export let placeholder: string = ''
	export let icon: IconDefinition | undefined = undefined
	export let rows: number = 2
	export let classes: string = ''

	export let id: string = ''
	export let name: string = ''
	export let required: boolean = false
</script>

<label class="{classes} flex flex-col justify-start gap-1">
	{#if name}
		<span class="uppercase text-xs tracking-widest">{name}</span>
	{/if}
	{#if icon}
		<div
			class="pl-4 flex items-center bg-transparent focus:ring-0 variant-soft-surface focus-within:border-primary-500 border-[1px] border-surface-500"
		>
			<Fa {icon} />
			<textarea
				{required}
				{name}
				{rows}
				{id}
				class="bg-transparent focus:ring-0 border-none focus-within:border-primary-500"
				{placeholder}
				bind:value
				on:keydown
				on:focusout
			/>
		</div>
	{:else}
		<textarea
			{required}
			{name}
			{rows}
			{id}
			class="bg-transparent focus:ring-0 variant-soft-surface focus-within:border-primary-500 border-[1px] border-surface-500"
			{placeholder}
			bind:value
			on:keydown
			on:focusout
		/>
	{/if}
</label>
