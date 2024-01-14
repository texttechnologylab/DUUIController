import { writable, type Writable } from 'svelte/store'
import type { DUUIComponent } from './duui/component'
import { type DUUIPipeline, blankPipeline } from './duui/pipeline'
import type { DUUIDocument } from './duui/io'
import { localStorageStore } from '@skeletonlabs/skeleton'

export const pipelineStore: Writable<DUUIPipeline[]> = writable([])
export const componentsStore: Writable<DUUIComponent[]> = writable([])
export const currentPipelineStore: Writable<DUUIPipeline> = writable(blankPipeline())
export const pipelineFilterStore: Writable<string[]> = writable([])
export const documentStore: Writable<DUUIDocument[]> = writable([])
export const markedForDeletionStore: Writable<string[]> = writable([])

export interface Session {
	session: string
	user: App.Locals['user']
}

export const userSession: Writable<User> = writable(undefined)
export const setLocale: Writable<Locale> = writable(undefined)
export const helpStore: Writable<boolean> = writable(false)

export const storage: Writable<Session> = localStorageStore('session', {
	session: '',
	user: {
		oid: '',
		authorization: '',
		session: '',
		email: '',
		role: 'user',
		preferences: {},
		connections: {}
	}
})
