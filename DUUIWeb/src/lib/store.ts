import { writable, type Writable } from 'svelte/store'
import { blankPipeline, type DUUIPipeline } from './duui/pipeline'

export const currentPipelineStore: Writable<DUUIPipeline> = writable(blankPipeline())

export const userSession: Writable<User> = writable(undefined)
export const helpStore: Writable<boolean> = writable(false)
