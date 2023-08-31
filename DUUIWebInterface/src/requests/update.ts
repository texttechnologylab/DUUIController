import type { DUUIPipeline } from '../Interfaces/interfaces';

async function updatePipelineName(pipeline: DUUIPipeline) {
	const data = JSON.stringify({ pipeline });

	fetch('http://127.0.0.1:9090/pipeline', {
		method: 'PUT',
		mode: 'cors',
		body: data
	})
		.then((response) => {
			return response.text();
		})
		.catch((error) => {
			console.error('Error:', error);
		});
}
