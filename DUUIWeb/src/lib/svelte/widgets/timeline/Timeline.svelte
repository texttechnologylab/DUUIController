<script lang="ts">
	import type { DUUIStatusEvent } from '$lib/duui/monitor'
	import { includes } from '$lib/utils/text'
	import { faSearch } from '@fortawesome/free-solid-svg-icons'
	import Search from '../input/Search.svelte'
	import Timestamp from './Timestamp.svelte'
	import Dropdown from '../input/Dropdown.svelte'

	export let title: string = 'Timeline'
	export let startTime: number
	export let events: DUUIStatusEvent[] = []

	export let vertical: boolean = false

	let query: string = ''
	let filteredEvents: DUUIStatusEvent[] = events

	let senders: Set<string> = new Set(events.map((e) => e.sender))
	let sender: string = 'Any'

	$: {
		filteredEvents = events.filter(
			(event: DUUIStatusEvent) => includes(`${event.sender} ${event.message}`, query) || !query
		)

		senders = new Set(events.map((e) => e.sender))
	}
</script>

{#if vertical}
	<div class="bg-surface-100 dark:variant-soft-surface shadow-lg p-4 space-y-4">
		<div class="grid md:flex md:justify-between gap-4">
			<h3 class="h3">{title}</h3>
			<div class="grid md:grid-cols-2 gap-4">
				<Dropdown
					options={[...senders, 'Any'].sort((a, b) => (a > b ? 1 : -1))}
					bind:value={sender}
				/>
				<Search bind:query icon={faSearch} placeholder="Search..." />
			</div>
		</div>
		<p>{query}</p>
		<hr class="bg-surface-400/20 h-[1px] !border-0 rounded" />
		<div
			class="
		 py-8
		 timeline
		 relative
         after:absolute
		 isolate
         after:w-2
		 after:z-[-1]
         after:bg-primary-500
         after:rounded-full
         after:top-4 after:bottom-4 after:left-1/2 after:-translate-x-1/2"
		>
			{#each filteredEvents as event, index}
				{#if index % 2 === 0}
					<div
						class="container left px-4 relative w-1/2 left-0
				    after:absolute after:w-6 after:aspect-square after:rounded-full after:border-2 after:border-primary-500 after:bg-white dark:after:bg-surface-900
				    after:top-1/2 after:-translate-y-1/2 after:-right-[12px] after:z-10
				    before:absolute before:h-0 before:w-0 before:z-10
					before:top-1/2 before:left-[30px] md:before:left-auto md:before:right-[22px] before:-translate-y-1/2
					before:border-l-0 before:border-r-[10px]
					md:before:border-l-[10px] md:before:border-r-0 before:border-t-[10px] before:border-b-[10px]
					md:before:border-l-white md:before:border-r-transparent before:border-t-transparent before:border-b-transparent
					before:border-l-transparent before:border-r-white
					md:dark:before:border-l-surface-900 dark:before:border-r-surface-900"
					>
						<div class="ml-4 md:mr-4 md:ml-0 shadow-lg bg-surface-50/100 dark:bg-surface-900">
							<Timestamp {startTime} {event} {vertical} />
						</div>
					</div>
				{:else}
					<div
						class="container right p-4 relative w-1/2 left-1/2
			 	    after:absolute after:w-6 after:aspect-square after:rounded-full after:border-2 after:border-primary-500 after:bg-white dark:after:bg-surface-900
        		    after:top-1/2 after:-translate-y-1/2 after:-left-[12px] after:z-10
				    before:absolute before:h-0 before:w-0 before:z-10
					before:top-1/2 before:left-[30px] md:before:left-[22px] before:-translate-y-1/2
					before:border-r-[10px] before:border-l-0 before:border-t-[10px] before:border-b-[10px]
				  before:border-r-white before:border-l-transparent before:border-t-transparent before:border-b-transparent
					dark:before:border-r-surface-900"
					>
						<div class="ml-4 shadow-lg bg-surface-50/100 dark:bg-surface-900">
							<Timestamp {startTime} {event} reversed={index % 2 === 1} {vertical} />
						</div>
					</div>
				{/if}
			{/each}
		</div>
	</div>
{:else}
	<div class="bg-surface-100 dark:variant-soft-surface shadow-lg p-4 space-y-4">
		<div class="grid md:flex md:justify-between gap-4">
			<h3 class="h3">{title}</h3>
			<div class="grid md:grid-cols-2 gap-4">
				<Dropdown
					options={[...senders, 'Any'].sort((a, b) => (a > b ? 1 : -1))}
					bind:value={sender}
				/>
				<Search bind:query icon={faSearch} placeholder="Search..." />
			</div>
		</div>
		<hr class="bg-surface-400/20 h-[1px] !border-0 rounded" />
		<div
			class="snap-x scroll-px-4 snap-mandatory scroll-smooth flex gap-2 overflow-x-auto pb-4 min-h-[16rem]"
		>
			{#each filteredEvents as event, index}
				<Timestamp {startTime} {event} />
				{#if index < filteredEvents.length - 1}
					<div class="inline-block my-auto border-b-4 border-dotted scale-[150%] opacity-50 px-4 border-primary-500 dark:border-surface-300" />
				{/if}
			{/each}
		</div>
	</div>
{/if}

<style>
	@media screen and (max-width: 768px) {
		.timeline::after {
			left: 12px;
		}

		.container {
			width: 100%;
			padding-left: 24px;
			padding-right: 0;
		}

		/* Make sure that all arrows are pointing leftwards */
		/* .container::before {
			left: 30px;
			border: solid white;
			border-width: 10px 10px 10px 0;
			border-color: transparent white transparent transparent;
			--tw-border-opacity: 1;
			border-right-color: rgb(var(--color-surface-900) / var(--tw-border-opacity));
		} */

		/* Make sure all circles are at the same spot */
		.left::after,
		.right::after {
			left: 0;
		}

		/* Make all right containers behave like the left ones */
		.right {
			left: 0%;
		}
	}
</style>
