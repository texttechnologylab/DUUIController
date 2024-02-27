import { v4 as uuidv4 } from 'uuid'

export const DUUIRemoteDriver = 'DUUIRemoteDriver'
export const DUUIDockerDriver = 'DUUIDockerDriver'
export const DUUISwarmDriver = 'DUUISwarmDriver'
export const DUUIUIMADriver = 'DUUIUIMADriver'
export const DUUIKubernetesDriver = 'DUUIKubernetesDriver'

export type DUUIDriver =
	| 'DUUIRemoteDriver'
	| 'DUUIDockerDriver'
	| 'DUUISwarmDriver'
	| 'DUUIUIMADriver'
	| 'DUUIKubernetesDriver'

export type DUUIDriverFilter = DUUIDriver | 'Any'
export const DUUIDriverFilters = [
	'Any',
	'DUUIRemoteDriver',
	'DUUIDockerDriver',
	'DUUISwarmDriver',
	'DUUIUIMADriver',
	'DUUIKubernetesDriver'
]

export const DUUIDrivers: string[] = [
	DUUIRemoteDriver,
	DUUIDockerDriver,
	DUUISwarmDriver,
	DUUIUIMADriver,
	DUUIKubernetesDriver
]

export type componentOptions =
	| 'use_GPU'
	| 'docker_image_fetching'
	| 'scale'
	| 'registry_auth'
	| 'constraints'
	| 'labels'
	| 'host'
	| 'ignore_200_error'

export interface DUUIComponent {
	oid: string
	id: string // Used for Drag & Drop
	name: string
	description: string
	tags: string[]
	driver: DUUIDriver
	target: string
	options: {
		use_GPU: boolean
		docker_image_fetching: boolean
		scale: number
		keep_alive: boolean
		registry_auth: {
			username: string
			password: string
		}
		constraints: string[]
		labels: string[]
		host: string
		ignore_200_error: boolean
	}
	parameters: any
	pipeline_id: string | null
	user_id: string | null
	index: number
}

export const blankComponent = (pipelineId: string, index: number) =>
	<DUUIComponent>{
		oid: uuidv4(),
		id: uuidv4(),
		name: 'New Component ' + (index + 1),
		tags: [],
		description: '',
		driver: DUUIDockerDriver,
		target: '',
		options: {
			use_GPU: true,
			docker_image_fetching: true,
			scale: 1,
			keep_alive: false,
			registry_auth: {
				username: '',
				password: ''
			},
			constraints: [],
			labels: [],
			host: '',
			ignore_200_error: true
		},
		parameters: {},
		pipeline_id: pipelineId,
		user_id: null,
		index: index
	}

export const componentToJson = (component: DUUIComponent) => {
	return {
		name: component.name,
		tags: component.tags,
		description: component.description,
		driver: component.driver,
		target: component.target,
		options: component.options,
		parameters: component.parameters,
		index: component.index
	}
}
