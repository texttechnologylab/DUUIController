import { v4 as uuidv4 } from 'uuid'

export interface DUUIPipelineComponent {
	id: string // relevant for DND
	name: string
	category: string
	driver: string
	target: string
	description: string
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
	Setup = 'setup',
	Completed = 'completed',
	Failed = 'failed',
	Running = 'running',
	Cancelled = 'cancelled',
	Unknown = 'unknown'
}

export const dummyPipeline: DUUIPipeline = {
	id: '1',
	name: 'Example',
	isNew: true,
	components: [
		{
			id: '1',
			name: 'Language Detection',
			category: 'Language',
			driver: DUUIRemoteDriver,
			target: 'http://127.0.0.1:8000',
			description: '',
			options: new Map<string, string>()
		},
		{
			id: '2',
			name: 'BreakIteratorSegmenter',
			category: 'Language',
			driver: DUUIUIMADriver,
			target: 'de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter',
			description: '',
			options: new Map<string, string>()
		},
		{
			id: '3',
			name: 'XMIWriter',
			category: 'Language',
			driver: DUUIUIMADriver,
			target: 'org.dkpro.core.io.XMIWriter',
			description: '',
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
		category: '',
		driver: DUUIDockerDriver,
		target: '',
		description: '',
		options: new Map<string, string>()
	}
