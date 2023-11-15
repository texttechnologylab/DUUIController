<script lang="ts">
	import { toTitleCase } from '$lib/utils/text'
	import { faClose } from '@fortawesome/free-solid-svg-icons'
	import Fa from 'svelte-fa'

	export let label: string
	export let values: string[]
	export let style: string = ''

	let current: string = ''

	const push = () => {
		if (current.length > 0 && !values.includes(current)) {
			values = [...values, toTitleCase(current)]
		}
		current = ''
	}

	const discard = (value: string) => {
		values = values.filter((v) => v !== value)
	}
</script>

<div class="label flex flex-col {style}">
	<span class="uppercase text-xs tracking-widest">{label}</span>

	<div
		class="flex flex-col border-[1px] bg-white dark:bg-surface-600 border-surface-400/20 focus-within:ring-1 focus-within:ring-[#2563eb] focus-within:border-[#2563eb]"
	>
		<input
			class="{values.length > 0
				? 'ring-0'
				: ''} border-none appearance-none ring-0 bg-transparent focus:ring-0 outline-none"
			type="text"
			bind:value={current}
			placeholder="Category"
			on:keypress={(event) => {
				if (event.key === 'Enter') {
					push()
				}
			}}
		/>
		<div class={values.length === 0 ? 'invisible' : 'flex flex-wrap gap-2 p-2'}>
			{#each values as value}
				<!-- svelte-ignore a11y-no-static-element-interactions -->
				<button class="chip variant-glass-primary" on:click={() => discard(value)}
					><span>
						{value}
					</span>
					<Fa icon={faClose} size="xs" />
				</button>
			{/each}
		</div>
	</div>
</div>
