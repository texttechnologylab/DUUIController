export interface DUUIDriver {
	name: string;
	short: string;
}

export class DUUIRemoteDriver implements DUUIDriver {
	name: string = 'DUUIRemoteDriver';
	short: string = 'Remote';
}

export class DUUIDockerDriver implements DUUIDriver {
	name: string = 'DUUIDockerDriver';
	short: string = 'Docker';
}

export class DUUISwarmDriver implements DUUIDriver {
	name: string = 'DUUISwarmDriver';
	short: string = 'Swarm';
}

export class DUUIUIMADriver implements DUUIDriver {
	name: string = 'DUUIUIMADriver';
	short: string = 'UIMA';
	withDebug: boolean = false;
}

export interface DUUIPipelineComponent {
	id: string;
	name: string;
	driver: string;
	target?: string | undefined;

}

export interface Template {
	name: string;
	timesUsed: number;
	createdAt: Date;
	components: DUUIPipelineComponent[];
}

export interface DUUIPipeline {
	id: string
	name: string;
	components: DUUIPipelineComponent[];
}

export const driverTypes: string[] = [
	'DUUIDockerDriver',
	'DUUIRemoteDriver',
	'DUUISwarmDriver',
	'DUUIUIMADriver'
];
