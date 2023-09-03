import { server_address } from '../lib/api/config';
import type { DUUIPipeline, DUUIPipelineComponent } from '../Interfaces/interfaces';

export async function insertPipeline(name: string, components: DUUIPipelineComponent[]) {
	fetch(server_address + '/pipeline', {
		method: 'POST',
		mode: 'cors',
		body: JSON.stringify({
			name: name,
			components: components
		})
	})
		.then((response) => {
			return response.text();
		})
		.catch((error) => {
			console.log(error);
			return '[Error]: ' + error;
		});
}

export async function insertComponent(pipelineId: string, component: DUUIPipelineComponent) {
	fetch(server_address + '/pipeline/component/' + pipelineId, {
		method: 'POST',
		mode: 'cors',
		body: JSON.stringify(component)
	})
		.then((response) => {
			return response.text();
		})
		.catch((error) => {
			console.log(error);
			return '[Error]: ' + error;
		});
}
