<!--
	@component
	A modal component that displays component templates.
-->
<script lang="ts">
	import type { DUUIComponent } from '$lib/duui/component'
	import { getDrawerStore, getModalStore, type DrawerSettings } from '@skeletonlabs/skeleton'
	import ComponentTemplates from '../ComponentTemplates.svelte'
	import { v4 as uuidv4 } from 'uuid'
	import pkg from 'lodash'
	import { currentPipelineStore } from '$lib/store'
	import { componentDrawerSettings } from '$lib/config'
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
			...componentDrawerSettings,
			meta: {
				component: copy,
				inEditor: false,
				creating: true
			}
		})
		modalStore.close()
	}
</script>

<div class="section-wrapper !bg-surface-100-800-token bordered-soft p-4 w-full md:w-modal-wide">
	<div>
		<ComponentTemplates
			components={templates}
			on:select={(event) => selectTemplate(event.detail.component)}
		/>
	</div>
</div>
