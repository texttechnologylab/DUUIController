import { writable, type Writable } from 'svelte/store'
import { blankPipeline, type DUUIPipeline } from './duui/pipeline'
import { blankComponent, type DUUIComponent } from './duui/component'
import { localStorageStore } from '@skeletonlabs/skeleton'

export const currentPipelineStore: Writable<DUUIPipeline> = localStorageStore(
	'currentPipelineStore',
	blankPipeline()
)
export const exampleComponent: Writable<DUUIComponent> = writable(blankComponent('', 0))
export const isDarkModeStore: Writable<boolean> = writable(false)

export const userSession: Writable<User> = writable(undefined)
