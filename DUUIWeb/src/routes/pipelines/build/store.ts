import { blankComponent, type DUUIComponent } from '$lib/duui/component'
import { writable } from 'svelte/store'

export const componentStore = writable<DUUIComponent>(blankComponent('', -1))
export const optionsStore = writable<Map<string, string>>(new Map())
