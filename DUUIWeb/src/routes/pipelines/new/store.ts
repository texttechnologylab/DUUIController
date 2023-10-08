import { blankComponent, type DUUIPipelineComponent } from '$lib/data'
import { writable } from 'svelte/store'

export const componentStore = writable<DUUIPipelineComponent>(blankComponent(-1))
export const optionsStore = writable<Map<string, string>>(new Map())
