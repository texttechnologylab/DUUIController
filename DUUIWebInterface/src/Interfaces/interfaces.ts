export interface DUUIDriver {
	displayName: string;
	short: string;
}

export class DUUIRemoteDriver implements DUUIDriver {
	displayName: string = 'DUUIRemoteDriver';
	short: string = 'Remote'
}

export class DUUIDockerDriver implements DUUIDriver {
	displayName: string = 'DUUIDockerDriver';
	short: string = 'Docker'
}

export class DUUISwarmDriver implements DUUIDriver {
	displayName: string = 'DUUISwarmDriver';
	short: string = 'Swarm'
}

export class DUUIUIMADriver implements DUUIDriver {
	displayName: string = 'DUUIUIMADriver';
	short: string = 'UIMA'
	withDebug: boolean = false;
}

export interface DUUIPipelineComponent {
	driver: DUUIDriver;
	displayName: string;
	id: string | null;
}

export interface Template {
	displayName: string;
	timesUsed: number;
	createdAt: Date;
	components: DUUIPipelineComponent[];
}

export interface DUUIPipeline {
	name: string,
	components: DUUIPipelineComponent[],
	
}