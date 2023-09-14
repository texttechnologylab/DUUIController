import type { Actions, PageServerLoad } from './$types'

export const load: PageServerLoad = async ({ params }) => {
	const loadPipeline = async () => {
		const result = await fetch('http://127.0.0.1:2605/pipelines/' + params.id, {
			method: 'GET',
			mode: 'cors'
		})
		return await result.json()
	}

	return {
		pipeline: loadPipeline()
	}
}

export const actions: Actions = {
	default: async ({ request }) => {
		const formData = await request.formData();
		console.log(formData);
		
	}
};
