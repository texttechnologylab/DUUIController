import { server_address } from './config';

export async function getPipeline(id: string): Promise<Response> {
	const result = await fetch(server_address + '/pipeline?id=' + id, {
		method: 'GET',
	});

	if (result.ok) {
		return result;
	} else {
		console.log("[Error]: Couldn't retrieve pipeline");
		return result;
	}
}

export async function getComponent(id: string) {
	fetch(server_address + '/pipeline/component' + id, {
		method: 'GET',
	})
		.then((response) => response.text())
		.then((data) => {
			return JSON.parse(data);
		})
		.catch((error) => {
			console.log(error);
			return '[Error]: ' + error;
		});
}

export async function getPipelineStatus(id: string) {
	fetch(server_address + '/pipeline/' + id + '/status', {
		method: 'GET',
		mode: 'cors'
	})
		.then((response) => {
			return response.text();
		})
		.catch((error) => {
			console.log(error);
			return '[Error]: ' + error;
		});
}

export async function getPipelines() {
	const result = await fetch(server_address + '/pipelines', {
		method: 'GET',
		mode: 'cors'
	});

	if (result.ok) {
		return await result.json();
	} else {
		return [];
	}
}
