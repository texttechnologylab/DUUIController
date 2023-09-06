import type { PageLoad } from '../$types'

export const load: PageLoad = async ({ params }) => {
	const loadPipeline = async () => {
		const result = await fetch('http://192.168.2.122:2605/pipeline/' + params.id, {
			method: 'GET',
			mode: 'cors'
		})
		return await result.json()
	}

	return {
		pipeline: loadPipeline()
	}
}
