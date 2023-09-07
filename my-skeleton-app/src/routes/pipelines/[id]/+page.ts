import type { PageLoad } from '../$types'

export const load: PageLoad = async ({ params }) => {
	const loadPipeline = async () => {
		const result = await fetch('http://127.0.0.1:2605/pipeline/' + params.id, {
			method: 'GET',
			mode: 'cors'
		})
		return await result.json()
	}

	return {
		pipeline: loadPipeline()
	}
}
