import { writable, type Writable } from 'svelte/store'
import { blankPipeline, type DUUIPipeline } from './duui/pipeline'
import { blankComponent, type DUUIComponent } from './duui/component'

export const currentPipelineStore: Writable<DUUIPipeline> = writable(blankPipeline())
export const exampleComponent: Writable<DUUIComponent> = writable(blankComponent('', 0))
export const isDarkModeStore: Writable<boolean> = writable(false)

export const userSession: Writable<User> = writable(undefined)
