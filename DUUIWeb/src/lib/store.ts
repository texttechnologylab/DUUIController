import { writable, type Writable } from 'svelte/store'
import { blankPipeline, type DUUIPipeline } from './duui/pipeline'
import { blankComponent, type DUUIComponent } from './duui/component'
import { localStorageStore } from '@skeletonlabs/skeleton'

// Stores the current state of the pipelines that is edited and used the brower's local storage.
export const currentPipelineStore: Writable<DUUIPipeline> = localStorageStore(
	'currentPipelineStore',
	blankPipeline()
)

export const feedbackStore: Writable<Feedback> = localStorageStore('feedback', {
	name: '',
	message: '',
	step: 0,
	language: 'english',
	programming: -1,
	java: -1,
	python: -1,
	duui: false,
	duuiRating: -1,
	nlp: -1,
	nlpNeeded: false,
	requirements: -1,
	frustration: -1,
	correction: -1,
	ease: -1
})

// Used in the documentation for illustration purposes.
export const exampleComponent: Writable<DUUIComponent> = writable(blankComponent('', 0))

// Store that tracks dark mode. Used for charts in both chart.ts files to check for dark mode.
export const isDarkModeStore: Writable<boolean> = writable(false)

// Store for the logged in User.
export const userSession: Writable<User> = writable(undefined)
