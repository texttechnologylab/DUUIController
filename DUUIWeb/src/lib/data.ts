import { v4 as uuidv4 } from 'uuid'

export interface DUUIPipelineComponent {
	id: string // relevant for DND
	name: string
	driver: string
	target: string
	options: Map<string, string> // Advanced Settings
}

export interface DUUIPipeline {
	id: string
	name: string
	isNew: boolean
	components: DUUIPipelineComponent[]
}

export interface DUUIProcess {
	id: string
	status: DUUIStatus
	progress: number
	pipeline_id: string
	options: Map<string, string>
}

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

export enum DUUIStatus {
	Completed = 'Completed',
	Failed = 'Failed',
	Running = 'Running'
}

export const dummyPipeline: DUUIPipeline = {
	id: '1',
	name: 'Example',
	isNew: true,
	components: [
		{
			id: '1',
			name: 'Language Detection',
			driver: DUUIRemoteDriver,
			target: 'http://127.0.0.1:8000',
			options: new Map<string, string>()
		},
		{
			id: '2',
			name: 'BreakIteratorSegmenter',
			driver: DUUIUIMADriver,
			target: 'de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter',
			options: new Map<string, string>()
		},
		{
			id: '3',
			name: 'XMIWriter',
			driver: DUUIUIMADriver,
			target: 'org.dkpro.core.io.XMIWriter',
			options: new Map<string, string>()
		}
	]
}

export const blankPipeline = () =>
	<DUUIPipeline>{
		id: uuidv4(),
		name: 'New Pipeline',
		isNew: true,
		components: []
	}

export const blankComponent = (id: string) =>
	<DUUIPipelineComponent>{
		id: id,
		name: 'New Component',
		driver: DUUIDockerDriver,
		target: '',
		options: new Map<string, string>()
	}
