import type { DUUIPipeline } from '../Interfaces/interfaces';

export async function updatePipeline(pipeline: DUUIPipeline) {
	fetch('http://127.0.0.1:9090/pipeline', {
		method: 'PUT',
		mode: 'cors',
		body: JSON.stringify(pipeline)
	})
		.then((response) => {
			return response.text();
		})
		.catch((error) => {
			console.error('Error:', error);
		});
}
