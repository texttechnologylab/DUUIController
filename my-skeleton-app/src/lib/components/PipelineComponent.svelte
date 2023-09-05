<script lang="ts">
	import DriverIcon from './DriverIcon.svelte'
	import type { DUUIPipelineComponent } from '$lib/data'
	import { createEventDispatcher } from 'svelte'
	import Fa from 'svelte-fa'
	import { faEdit } from '@fortawesome/free-solid-svg-icons'
	const dispatcher = createEventDispatcher()
	export let component: DUUIPipelineComponent

	let onRemove = () => {
		dispatcher('remove', {
			id: component.id
		})
		dialog.close()
	}

	let dialog: HTMLDialogElement
	let expanded: boolean = false

	const onEditComponent = () => {}
</script>

<li class="card grid gap-4 p-4 items-start">
	<div class="flex">
		<div class="grow flex flex-col items-start">
			<p class="h4">{component.name}</p>
		</div>
		<DriverIcon driver={component.driver} />
	</div>

	<button class="btn-icon variant-glass-surface" on:click={() => onEditComponent()}>
		<span>
			<Fa size="md" icon={faEdit} />
		</span>
	</button>
	<p class="hidden md:block max-w-[10ch]">{component.target}</p>
</li>

<style>
	.grid-container {
		display: grid;
		grid-template-rows: 0fr;

		transition: grid-template-rows 500ms ease-out;
	}

	.grid-container[aria-expanded='true'] {
		grid-template-rows: 1fr;
	}
</style>
