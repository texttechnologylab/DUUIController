import type { PageServerLoad } from './$types';

export const load: PageServerLoad = async () => {
	const loadPipelines = async () => {
		const result = await fetch('http://127.0.0.1:2605/pipelines', {
			method: 'GET',
			mode: 'cors'
		});
		return await result.json();
	};

	return {
		pipelines: loadPipelines()
	};
};
