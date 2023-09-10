import type { DUUIPipeline } from '$lib/data'
import type { PageServerLoad } from './$types'

export const load: PageServerLoad = async ({ fetch, params }) => {
	const loadPipelines = async (): Promise<{ pipelines: DUUIPipeline[] }> => {
		const result = await fetch('http://127.0.0.1:2605/pipelines', {
			method: 'GET',
			mode: 'cors'
		})
		return await result.json()
	}

	return {
		pipelines: (await loadPipelines()).pipelines
	}
}
