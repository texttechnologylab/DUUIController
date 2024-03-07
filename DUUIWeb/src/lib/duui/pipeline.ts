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

/**
 * Create and return a default pipeline.
 *
 * @returns A default pipeline with no components.
 */
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

/**
 * Convert a DUUIPipeline to a JSON object with some properties omitted.
 *
 * @param pipeline the pipeline to convert
 * @returns An object with relevant pipeline properties.
 */
export const pipelineToJson = (pipeline: DUUIPipeline) => {
	return {
		name: pipeline.name,
		description: pipeline.description,
		settings: pipeline.settings,
		components: pipeline.components.map((component) => componentToJson(component))
	}
}

/**
 * Retrieve a set of drivers that are used by components in the pipeline.
 *
 * @param pipeline The pipeline to get the drivers for
 * @returns A set of DUUIDrivers
 */
export const usedDrivers = (pipeline: DUUIPipeline) =>
	new Set(pipeline.components.map((c) => c.driver))

/**
 * Retrieve all pipeline tags as a string.
 *
 * @param pipeline The pipeline to get the tags for.
 * @returns A string containing all tags in the pipeline separated by spaces.
 */
export const getPipelineTagsAsString = (pipeline: DUUIPipeline) => {
	return pipeline.components.map((c) => c.tags.join(' ')).join(' ')
}
