import { v4 as uuidv4 } from 'uuid'

export interface DUUIPipelineComponent {
	id: string
	name: string
	driver: string
	target: string
	pipeline_id: string
}

export interface DUUIPipeline {
	id: string
	name: string
	status: string
	components: DUUIPipelineComponent[]
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

export const dummyPipeline: DUUIPipeline = {
	id: '1',
	name: 'Example',
	status: 'Inactive',
	components: [
		{
			id: '1',
			name: 'Language Detection',
			driver: DUUIRemoteDriver,
			target: 'http://127.0.0.1:8000',
			pipeline_id: '1'
		},
		{
			id: '2',
			name: 'BreakIteratorSegmenter',
			driver: DUUIUIMADriver,
			target: 'de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter',
			pipeline_id: '1'
		},
		{
			id: '3',
			name: 'XMIWriter',
			driver: DUUIUIMADriver,
			target: 'org.dkpro.core.io.XMIWriter',
			pipeline_id: '1'
		}
	]
}

export const dummyPipeline2: DUUIPipeline = {
	id: '2',
	name: 'Example 2',
	status: 'Inactive',
	components: [
		{
			id: '1',
			name: 'Language Detection',
			driver: DUUIRemoteDriver,
			target: 'http://127.0.0.1:8000',
			pipeline_id: '2'
		},
		{
			id: '2',
			name: 'BreakIteratorSegmenter',
			driver: DUUIUIMADriver,
			target: 'de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter',
			pipeline_id: '2'
		},
		{
			id: '3',
			name: 'XMIWriter',
			driver: DUUIUIMADriver,
			target: 'org.dkpro.core.io.XMIWriter',
			pipeline_id: '2'
		}
	]
}

export const dummyPipeline3: DUUIPipeline = {
	id: '3',
	name: 'Example 3',
	status: 'Inactive',
	components: [
		{
			id: '1',
			name: 'Language Detection',
			driver: DUUIUIMADriver,
			target: '',
			pipeline_id: '3'
		},
		{
			id: '2',
			name: 'spaCy3',
			driver: DUUIRemoteDriver,
			target: '',
			pipeline_id: '3'
		},
		{
			id: '3',
			name: 'GNFinder',
			driver: DUUIDockerDriver,
			target: '',
			pipeline_id: '3'
		},
		{
			id: '4',
			name: 'TaxonGazetter',
			driver: DUUIRemoteDriver,
			target: '',
			pipeline_id: '3'
		},
		{
			id: '5',
			name: 'SentimentBERT',
			driver: DUUIDockerDriver,
			target: '',
			pipeline_id: '3'
		},
		{
			id: '6',
			name: 'SRL',
			driver: DUUISwarmDriver,
			target: '',
			pipeline_id: '3'
		},
		{
			id: '7',
			name: 'XMIWriter',
			driver: DUUIUIMADriver,
			target: '',
			pipeline_id: '3'
		}
	]
}

export const blankPipeline = () =>
	<DUUIPipeline>{
		id: uuidv4(),
		name: 'New Pipeline',
		status: 'New',
		components: []
	}

export const blankComponent = (pipeline_id: string, name?: string) =>
	<DUUIPipelineComponent>{
		id: uuidv4(),
		name: typeof name === undefined ? 'New Component' : name,
		driver: DUUIDockerDriver,
		target: '',
		pipeline_id: pipeline_id
	}

export const dummyPipelineList: DUUIPipeline[] = [dummyPipeline, dummyPipeline2, dummyPipeline3]
