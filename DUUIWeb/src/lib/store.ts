import { localStorageStore } from '@skeletonlabs/skeleton'
import { writable, type Writable } from 'svelte/store'
import { blankComponent, type DUUIComponent } from './duui/component'
import { blankPipeline, type DUUIPipeline } from './duui/pipeline'
import { blankSettings, type ProcessSettings } from './duui/process'

// Stores the current state of the pipelines that is edited and used the brower's local storage.
export const currentPipelineStore: Writable<DUUIPipeline> = localStorageStore(
	'currentPipelineStore',
	blankPipeline()
)

export const examplePipelineStore: Writable<DUUIPipeline> = writable(blankPipeline())

// Store the state of the feedback form
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

// Stores the current state of the process settings.
export const processSettingsStore: Writable<ProcessSettings> = writable(blankSettings())

// Used in the documentation for illustration purposes.
export const exampleComponent: Writable<DUUIComponent> = writable(blankComponent('', 0))

// Store that tracks dark mode. Used for charts in both chart.ts files to check for dark mode.
export const isDarkModeStore: Writable<boolean> = writable(false)

// Store for the logged in User.
export const userSession: Writable<User> = writable(undefined)
