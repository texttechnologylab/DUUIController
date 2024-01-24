import type { _Object } from '$lib/config'
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

export type DUUIDriverFilter = DUUIDriver | 'All'
export const DUUIDriverFilters = [
	'All',
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

export type SvelteComponentWrapper = {
	component: DUUIComponent
	id: string
}

export interface DUUIComponent {
	oid: string
	id: string // Drag & Drop
	name: string
	description: string
	tags: string[]
	status: string
	driver: DUUIDriver
	target: string
	options: any
	parameters: any
	pipelineId: string | null
	userId: string | null
	index: number
}


export const blankComponent = (pipelineId: string, index: number) =>
	<DUUIComponent>{
		oid: uuidv4(),
		id: uuidv4(),
		name: 'New Component ' + index,
		tags: [],
		description: '',
		status: '',
		driver: DUUIDockerDriver,
		target: '',
		options: {},
		parameters: {},
		pipelineId: pipelineId,
		userId: null,
		index: index
	}

export const componentToJson = (component: DUUIComponent) => {
	return {
		name: component.name,
		categories: component.tags,
		description: component.description,
		driver: component.driver,
		target: component.target,
		options: component.options,
		parameters: component.parameters,
		index: component.index
	}
}
