import { server_address } from '.';
import type { DUUIPipeline, DUUIPipelineComponent } from '../Interfaces/interfaces';

async function addPipeline(pipeline: DUUIPipeline) {
	fetch(server_address + '/pipeline/', {
		method: 'POST',
		mode: 'cors',
		body: JSON.stringify(pipeline)
	})
		.then((response) => {
			return response.text();
		})
		.catch((error) => {
			console.log(error);
			return '[Error]: ' + error;
		});
}

async function addComponent(pipelineId: string, component: DUUIPipelineComponent) {
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
