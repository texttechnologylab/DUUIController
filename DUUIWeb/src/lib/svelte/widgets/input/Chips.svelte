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

	<div class="input-wrapper !focus-within:ring-red-400 flex flex-col">
		<input
			class="{values.length > 0
				? 'ring-0'
				: ''} border-none appearance-none ring-0 bg-transparent focus:ring-0"
			type="text"
			bind:value={current}
			{placeholder}
			on:keypress={(event) => {
				if (event.key === 'Enter') {
					push()
				}
			}}
		/>
		<div class={values.length === 0 ? 'invisible' : 'flex flex-wrap gap-2 p-2'}>
			{#each values as value}
				<!-- svelte-ignore a11y-no-static-element-interactions -->
				<button class="chip variant-soft-primary" on:click={() => discard(value)}
					><span>
						{value}
					</span>
					<Fa icon={faClose} size="xs" />
				</button>
			{/each}
		</div>
	</div>
</div>
