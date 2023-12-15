<script lang="ts">
	import type { DUUIStatusEvent } from '$lib/duui/monitor'
	import { datetimeToString } from '$lib/utils/text'
	import { formatMilliseconds } from '$lib/utils/time'
	import { faBook } from '@fortawesome/free-solid-svg-icons'
	import Fa from 'svelte-fa'
	import { fly } from 'svelte/transition'

	export let startTime: number
	export let event: DUUIStatusEvent
	export let reversed: boolean = false
	export let vertical: boolean = false

	const timePassed: number = event.timestamp - startTime
</script>

{#if vertical}
	<div
		class="p-4 md:border-l-[8px] md:border-l-primary-500 border-r-[8px] border-r-primary-500 md:border-r-0"
	>
		<div class="flex items-center gap-4 justify-between uppercase tracking-wide">
			<p class="text-sm">{event.sender}</p>
			<p class="text-xs">{formatMilliseconds(timePassed)}</p>
		</div>
		<p class="pt-2">{event.message}</p>
	</div>
	{#if reversed}
		<div class="p-4 relative border-r-[8px] border-r-primary-500">
			<div class="flex items-center gap-4 justify-between uppercase tracking-wide">
				<p class="text-sm">{event.sender}</p>
				<p class="text-xs">{formatMilliseconds(timePassed)}</p>
			</div>
			<p class="pt-2">{event.message}</p>
		</div>
	{/if}
{:else}
	<div
		class="flex flex-col justify-between rounded-none space-y-2  border-2 bg-surface-50 dark:bg-surface-800 border-surface-200 dark:border-surface-500"
	>
		<div class="p-4 w-60 md:w-[20rem]">
			<div class="flex items-center justify-between gap-4">
				<p class="text-lg font-bold">{event.sender}</p>
				<Fa icon={faBook} />
			</div>
			<p class="text-sm pt-2">{event.message}</p>
		</div>
		<p
			class="text-xs w-full variant-soft-surface text-center bottom-0 py-3 border-t-2 border-t-surface-200  dark:border-surface-500"
		>
			{formatMilliseconds(timePassed)}
		</p>
	</div>
{/if}
