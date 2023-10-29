import type { _Object } from '$lib/config'
import { v4 as uuidv4 } from 'uuid'

export const DUUIRemoteDriver = 'DUUIRemoteDriver'
export const DUUIDockerDriver = 'DUUIDockerDriver'
export const DUUISwarmDriver = 'DUUISwarmDriver'
export const DUUIUIMADriver = 'DUUIUIMADriver'

export const DUUIDrivers: string[] = [
	DUUIRemoteDriver,
	DUUIDockerDriver,
	DUUISwarmDriver,
	DUUIUIMADriver
]

export interface DUUIComponent {
	id: string
	name: string
	categories: string[]
	description: string
	status: string
	settings: {
		driver: string
		target: string
		options: _Object
		parameters: _Object
	}
	pipelineId: string | null
	userId: string | null
	index: number | null
}

export const blankComponent = (pipelineId: string, index: number) =>
	<DUUIComponent>{
		id: uuidv4(),
		name: 'New Component ' + index,
		categories: [],
		description: '',
		status: '',
		settings: {
			driver: DUUIDockerDriver,
			target: '',
			options: {},
			parameters: {}
		},
		pipelineId: pipelineId,
		userId: null,
		index: index
	}
