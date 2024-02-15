<script lang="ts">
	import type { DUUIComponent } from '$lib/duui/component'
	import { getDrawerStore, getModalStore, type DrawerSettings } from '@skeletonlabs/skeleton'
	import ComponentTemplates from '../../../../routes/pipelines/build/ComponentTemplates.svelte'
	import { v4 as uuidv4 } from 'uuid'
	import pkg from 'lodash'
	import { currentPipelineStore } from '$lib/store'
	const { cloneDeep } = pkg

	const modalStore = getModalStore()
	const drawerStore = getDrawerStore()

	export let templates: DUUIComponent[] = $modalStore[0].meta['templates']

	const selectTemplate = (component: DUUIComponent) => {
		const copy = cloneDeep(component)
		copy.id = uuidv4()
		copy.index = $currentPipelineStore.components.length
		drawerStore.open({
			id: 'component',
			width: 'w-full 2xl:w-1/2',
			position: 'right',
			rounded: 'rounded-none',
			meta: {
				component: copy,
				inEditor: false,
				creating: true
			}
		})
		modalStore.close()
	}
</script>

<div class="section-wrapper p-4 h-full w-full md:w-modal-wide">
	<div class="h-full">
		<ComponentTemplates
			components={templates}
			on:select={(event) => selectTemplate(event.detail.component)}
		/>
	</div>
</div>
