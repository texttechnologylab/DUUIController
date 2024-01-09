import { v4 as uuidv4 } from 'uuid'
import { componentToJson, type DUUIComponent } from './component'
import type { _Object } from '$lib/config'

export interface DUUIPipeline {
	oid: string
	name: string
	description: string
	createdAt: number
	serviceStartTime: number
	timesUsed: number
	lastUsed: number | undefined
	settings: Object
	user_id: string | null | undefined // if null -> Template
	components: DUUIComponent[]
}

export const blankPipeline = () =>
	<DUUIPipeline>{
		oid: uuidv4(),
		name: 'New Pipeline',
		description: '',
		createdAt: Date.now(),
		serviceStartTime: 0,
		timesUsed: 0,
		lastUsed: 0,
		settings: {},
		user_id: null,
		components: []
	}

export const pipelineToExportableJson = (pipeline: DUUIPipeline) => {
	return {
		name: pipeline.name,
		description: pipeline.description,
		settings: pipeline.settings,
		components: pipeline.components.map((component) => componentToJson(component))
	}
}

export const usedDrivers = (pipeline: DUUIPipeline) =>
	new Set(pipeline.components.map((c) => c.settings.driver))

export const getPipelineCategories = (pipeline: DUUIPipeline) => {
	const categories = pipeline.components.map((c) => c.categories.join(' ')).join(' ')
	return categories
}
