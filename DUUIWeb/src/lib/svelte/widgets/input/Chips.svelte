<script lang="ts">
	import { includesText, toTitleCase } from '$lib/duui/utils/text'
	import { faClose } from '@fortawesome/free-solid-svg-icons'
	import { createEventDispatcher } from 'svelte'
	import Fa from 'svelte-fa'

	const dispatcher = createEventDispatcher()

	export let label: string
	export let values: string[]
	export let style: string = ''
	export let placeholder: string = 'Tag'

	let current: string = ''

	const push = () => {
		if (current.length > 0 && !includesText(values, current)) {
			values = [...values, toTitleCase(current)]
		}
		dispatcher('push', { text: current })
		current = ''
	}

	const discard = (value: string) => {
		values = values.filter((v) => v !== value)
		dispatcher('discard', { text: value })
	}
</script>

<div class="label flex flex-col {style}">
	<span class="form-label">{label}</span>

	<div
		class="input-wrapper !p-0 !focus-within:ring-red-400 flex flex-col gap-2 {values.length > 0
			? '!pb-0'
			: ''}"
	>
		<input
			class="{values.length > 0
				? 'ring-0'
				: ''} border-none appearance-none ring-0 bg-transparent focus:ring-0 py-3"
			type="text"
			bind:value={current}
			{placeholder}
			on:keypress={(event) => {
				if (event.key === 'Enter') {
					push()
				}
			}}
		/>
		<div class={values.length === 0 ? 'hidden' : 'flex flex-wrap gap-2 px-2 pb-2'}>
			{#each values.sort((a, b) => (a < b ? -1 : 1)) as value}
				<div class="flex items-center gap-1 chip variant-ghost-primary">
					<span>{value}</span>
					<button class="animate-text" on:click={() => discard(value)}>
						<Fa icon={faClose} />
					</button>
				</div>
			{/each}
		</div>
	</div>
</div>
