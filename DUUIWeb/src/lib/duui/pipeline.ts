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
	lastUsed: number | null
	settings: Object
	userId: string | null // if null -> Template
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
		lastUsed: null,
		settings: {},
		userId: null,
		components: []
	}

export const pipelineToJson = (pipeline: DUUIPipeline) => {
	return {
		name: pipeline.name,
		description: pipeline.description,
		settings: pipeline.settings,
		components: pipeline.components.map((component) => componentToJson(component))
	}
}

export const usedDrivers = (pipeline: DUUIPipeline) => {
	let drivers: Set<string> = new Set()

	for (let component of pipeline.components) {
		drivers.add(component.settings.driver)
	}

	return drivers
}
