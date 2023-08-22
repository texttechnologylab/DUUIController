interface DUUIDriver {
	displayName: string;
}

class DUUIRemoteDriver implements DUUIDriver {
	displayName: string = 'DUUIRemoteDriver';
}

class DUUIDockerDriver implements DUUIDriver {
	displayName: string = 'DUUIDockerDriver';
}

class DUUISwarmDriver implements DUUIDriver {
	displayName: string = 'DUUISwarmDriver';
}

class DUUIUIMADriver implements DUUIDriver {
	displayName: string = 'DUUIUIMADriver';
}

class DUUIPipelineComponent {
	driver: DUUIDriver;
	displayName: string;
	id: string | null = null;
	description: string | undefined;

	constructor(driver: DUUIDriver, displayName: string) {
		this.driver = driver;
		this.displayName = displayName;
		this.description = `A DUUIPipelineComponent ran by ${this.driver.displayName}`;
	}
}

interface Template {
	displayName: string;
	timesUsed: number;
	createdAt: Date;
	components: DUUIPipelineComponent[];
}
