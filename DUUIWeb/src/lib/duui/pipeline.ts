import { v4 as uuidv4 } from 'uuid'
import { componentToJson, type DUUIComponent } from './component'

export interface DUUIPipeline {
	oid: string
	name: string
	description: string
	status: string
	tags: string[]
	created_at: number
	modified_at: number
	times_used: number
	last_used: number | undefined
	settings: Object
	user_id: string | null // if null -> Template
	components: DUUIComponent[]
	statistics: {
		status: AggreationResult
		errors: AggreationResult
		input: AggreationResult
		output: AggreationResult
		usage: AggreationResult
		size: AggreationResult
	}
}

export const blankPipeline = () =>
	<DUUIPipeline>{
		oid: uuidv4(),
		name: 'New Pipeline',
		description: '',
		created_at: Date.now(),
		modified_at: Date.now(),
		times_used: 0,
		last_used: 0,
		settings: {},
		user_id: null,
		components: [],
		tags: [],
		status: 'Inactive',
		statistics: {
			status: [],
			errors: [],
			input: [],
			output: [],
			usage: [],
			size: []
		}
	}

	
export const pipelineToJson = (pipeline: DUUIPipeline) => {
	return {
		name: pipeline.name,
		description: pipeline.description,
		settings: pipeline.settings,
		components: pipeline.components.map((component) => componentToJson(component))
	}
}

export const usedDrivers = (pipeline: DUUIPipeline) =>
	new Set(pipeline.components.map((c) => c.driver))

export const getPipelineCategories = (pipeline: DUUIPipeline) => {
	const categories = pipeline.components.map((c) => c.tags.join(' ')).join(' ')
	return categories
}
