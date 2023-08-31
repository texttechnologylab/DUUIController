import { server_address } from '.';

export async function getPipeline(id: string) {
	const result = await fetch(server_address + '/pipeline/' + id, {
		method: 'GET',
		mode: 'cors'
	});

	if (result.ok) {
		return await result.text();
		return JSON.parse(await result.text());
	} else {
		return "[Error]: Couldn't retrieve pipeline";
	}
}

export async function getComponent(id: string) {
	fetch(server_address + '/pipeline/component' + id, {
		method: 'GET',
		mode: 'cors'
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
	const result = await fetch(server_address + '/pipeline', {
		method: 'GET',
		mode: 'cors'
	});

	if (result.ok) {
		return await result.text();
		return JSON.parse(await result.text());
	} else {
		return "[Error]: Couldn't retrieve pipelines";
	}
}
