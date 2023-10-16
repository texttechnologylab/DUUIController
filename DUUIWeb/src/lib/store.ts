import { writable, type Writable } from 'svelte/store'
import type { DUUIComponent } from './duui/component'
import { type DUUIPipeline, blankPipeline } from './duui/pipeline'

export const pipelineStore: Writable<DUUIPipeline[]> = writable([])
export const componentsStore: Writable<DUUIComponent[]> = writable([])
export const currentPipelineStore: Writable<DUUIPipeline> = writable(blankPipeline())
export const pipelineFilterStore: Writable<string[]> = writable([])
