import { v4 as uuidv4 } from 'uuid'

export interface DUUIPipeline {
	id: string
	name: string
	description: string
	createdAt: number
	isNew: boolean
	components: DUUIPipelineComponent[]
}

export interface DUUIPipelineComponent {
	id: number // Only relevant for DND in WebApp
	name: string
	category: string
	description: string
	settings: {
		driver: string
		target: string
		options: Map<string, string> // Advanced Settings
	}
}

export interface DUUIProcess {
	id: string
	status: DUUIStatus
	progress: number
	startedAt?: number
	finishedAt?: number
	input: {
		source: DUUIDocumentSource
		path?: string
		text?: string
	}
	output: {
		type: DUUIDocumentOutput
		path: string
	}
	options: Map<string, string>
	pipeline_id: string
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

export enum DUUIDocumentSource {
	Dropbox = 'Dropbox',
	Hessenbox = 'Hessenbox',
	Files = 'Local Files',
	S3 = "S3",
	None = 'None'
}

export const DUUIDocumentSourcesList = [
	DUUIDocumentSource.None,
	DUUIDocumentSource.Files,
	DUUIDocumentSource.Dropbox,
	DUUIDocumentSource.S3,
]

export enum DUUIDocumentOutput{
	Dropbox = 'Dropbox',
	Hessenbox = 'Hessenbox',
	Files = 'Local Files',
	S3 = "S3",
	None = 'None',
	Json = 'Json',
	CSV = 'CSV'
}

export const blankPipeline = () =>
	<DUUIPipeline>{
		id: uuidv4(),
		name: 'New Pipeline',
		description: '',
		createdAt: Date.now(),
		isNew: true,
		components: []
	}

export const blankComponent = (id: number) =>
	<DUUIPipelineComponent>{
		id: id,
		name: 'New Component',
		category: '',
		description: 'My new Component for...',
		settings: {
			driver: DUUIDockerDriver,
			target: '',
			options: new Map<string, string>()
		}
	}

export const blankProcess = (pipeline_id: string) =>
	<DUUIProcess>{
		id: uuidv4(),
		status: DUUIStatus.Setup,
		progress: 0,
		startedAt: undefined,
		finishedAt: undefined,
		input: {
			source: DUUIDocumentSource.None,
			path: '',
			text: ''
		},
		output: {
			type: DUUIDocumentOutput.None,
			path: ''
		},
		options: new Map<string, string>(),
		pipeline_id: pipeline_id
	}
