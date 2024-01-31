<script lang="ts">
	import DriverIcon from '$lib/svelte/DriverIcon.svelte'

	import pkg from 'lodash'
	const { cloneDeep } = pkg

	import { type DUUIComponent } from '$lib/duui/component'
	import { slugify } from '$lib/duui/utils/text'
	import { faClone, faEdit } from '@fortawesome/free-solid-svg-icons'
	import { getDrawerStore, type DrawerSettings } from '@skeletonlabs/skeleton'
	import { createEventDispatcher } from 'svelte'
	import Fa from 'svelte-fa'

	export let component: DUUIComponent

	export let inEditor: boolean = false
	export let cloneable: boolean = false

	const dispatcher = createEventDispatcher()

	const drawerStore = getDrawerStore()
	const drawer: DrawerSettings = {
		id: 'component',
		width: 'w-full lg:w-3/4',
		position: 'right',
		rounded: 'rounded-none',
		meta: { component: component, inEditor: inEditor }
	}
</script>

<li
	id={slugify(component.name)}
	class="section-wrapper scroll-mt-4 md:scroll-mt-24 pointer-events-auto"
>
	<header class="flex justify-between gap-4 items-center p-4 bg-surface-50-900-token">
		<div class="md:flex md:items-center grid gap-4">
			<DriverIcon driver={component.driver} />
			<p class="md:h4 grow">{component.name}</p>
		</div>
		<div class="scroll-mt-4 md:scroll-mt-16 flex-col-reverse gap-4 md:flex-row flex">
			{#if cloneable}
				<button
					class="pointer-events-auto animate-text"
					on:click={() => {
						dispatcher('clone', { component: cloneDeep(component) })
					}}
				>
					<Fa icon={faClone} size="lg" />
				</button>
			{/if}
			<button
				class="pointer-events-auto animate-text"
				on:click={() => {
					drawerStore.open(drawer)
				}}
			>
				<Fa icon={faEdit} size="lg" />
			</button>
		</div>
	</header>
</li>
