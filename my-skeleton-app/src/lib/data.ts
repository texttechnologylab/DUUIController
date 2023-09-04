import { v4 as uuidv4 } from 'uuid';

export interface DUUIPipelineComponent {
	id: string;
	name: string;
	driver: string;
	target: string;
	pipeline_id: string;
}

export interface DUUIPipeline {
	id: string;
	name: string;
	status: string;
	components: DUUIPipelineComponent[];
}

export const DUUIRemoteDriver = 'DUUIRemoteDriver';
export const DUUIDockerDriver = 'DUUIDockerDriver';
export const DUUISwarmDriver = 'DUUISwarmDriver';
export const DUUIUIMADriver = 'DUUIUIMADriver';

export const DUUIDrivers: string[] = [
	DUUIRemoteDriver,
	DUUIDockerDriver,
	DUUISwarmDriver,
	DUUIUIMADriver
];

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
			id: '1',
			name: 'BreakIteratorSegmenter',
			driver: DUUIUIMADriver,
			target: 'de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter',
			pipeline_id: '1'
		},
		{
			id: '1',
			name: 'XMIWriter',
			driver: DUUIUIMADriver,
			target: 'org.dkpro.core.io.XMIWriter',
			pipeline_id: '1'
		}
	]
};

export const blankPipeline = () =>
	<DUUIPipeline>{
		id: uuidv4(),
		name: 'New Pipeline',
		status: 'New',
		components: []
	};

export const blankComponent = (pipeline_id: string, name?: string) =>
	<DUUIPipelineComponent>{
		id: uuidv4(),
		name: typeof name === undefined ? 'New Component' : name,
		driver: DUUIDockerDriver,
		target: '',
		pipeline_id: pipeline_id
	};
