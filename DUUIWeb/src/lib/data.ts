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
		extension: string
	}
	output: {
		type: DUUIDocumentOutput
		path: string
		compression: string
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
	Running = 'running',
	Output = "output",
	Failed = 'failed',
	Cancelled = 'cancelled',
	Completed = 'completed',
	Unknown = 'unknown'
}

export enum DUUIDocumentSource {
	Dropbox = 'Dropbox',
	Hessenbox = 'Hessenbox',
	Files = 'Files',
	S3 = 'S3',
	Text = 'Text'
}

export const DUUIInputSourcesList = [
	DUUIDocumentSource.Text,
	DUUIDocumentSource.Files,
	DUUIDocumentSource.Dropbox,
	DUUIDocumentSource.S3
]

export enum DUUIDocumentOutput {
	CSV = 'CSV',
	Dropbox = 'Dropbox',
	Files = 'Files',
	Hessenbox = 'Hessenbox',
	Json = 'Json',
	None = 'None',
	S3 = 'S3',
	Text = 'Text'
}

export const DUUIOutputSourcesList = [
	DUUIDocumentOutput.CSV,
	DUUIDocumentOutput.Dropbox,
	DUUIDocumentOutput.Files,
	DUUIDocumentOutput.Hessenbox,
	DUUIDocumentOutput.Json,
	DUUIDocumentOutput.None,
	DUUIDocumentOutput.S3,
	DUUIDocumentOutput.Text
]

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
		name: '',
		category: '',
		description: '',
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
			source: DUUIDocumentSource.Text,
			path: '',
			text: '',
			extension: ''
		},
		output: {
			type: DUUIDocumentOutput.None,
			path: '',
			compression: ''
		},
		options: new Map<string, string>(),
		pipeline_id: pipeline_id
	}
