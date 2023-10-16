import type { _Object } from '$lib/config'

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
	id: number
	name: string
	category: string
	description: string
	saveAsTemplate: boolean
	settings: {
		driver: string
		target: string
		options: _Object
		parameters: _Object
	}
}

export const blankComponent = (id: number) =>
	<DUUIComponent>{
		id: id,
		name: 'New Component ' + id,
		category: '',
		description: '',
		saveAsTemplate: true,
		settings: {
			driver: DUUIDockerDriver,
			target: '',
			options: {},
			parameters: {}
		}
	}
