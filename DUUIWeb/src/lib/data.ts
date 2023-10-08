import { v4 as uuidv4 } from 'uuid'

interface _Object {
	[key: string]: any
}

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
		options: _Object // Advanced Settings
		parameters: _Object
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
	log: DUUIStatusEvent[]
	documentCount: number
	documentNames: string[]
}

export interface DUUIStatusEvent {
	timestamp: number
	sender: string
	message: string
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
	Output = 'output',
	Failed = 'failed',
	Cancelled = 'cancelled',
	Completed = 'completed',
	Unknown = 'unknown'
}

export enum DUUIDocumentSource {
	Dropbox = 'Dropbox',
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
	CSV = '.csv',
	Dropbox = 'Dropbox',
	Json = '.json',
	None = 'None',
	S3 = 'S3',
	Xmi = '.xmi',
	Text = '.txt'
}

export const DUUIOutputSourcesList = [
	DUUIDocumentOutput.S3,
	DUUIDocumentOutput.Dropbox,
	DUUIDocumentOutput.CSV,
	DUUIDocumentOutput.Json,
	DUUIDocumentOutput.Text,
	DUUIDocumentOutput.Xmi,
	DUUIDocumentOutput.None
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
		name: 'New Component ' + id,
		category: '',
		description: '',
		settings: {
			driver: DUUIDockerDriver,
			target: '',
			options: {},
			parameters: {}
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
		pipeline_id: pipeline_id,
		log: [],
		documentCount: 0,
		documentNames: []
	}
