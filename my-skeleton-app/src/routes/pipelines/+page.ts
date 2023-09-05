import type { DUUIPipeline } from '$lib/data'
import type { PageLoad } from './$types'

export const load: PageLoad = async ({ fetch, params }) => {
	const loadPipelines = async (): Promise<DUUIPipeline[]> => {
		const result = await fetch('http://192.168.2.122:2605/pipelines', {
			method: 'GET',
			mode: 'cors'
		})
		return await result.json()
	}

	return {
		pipelines: loadPipelines()
	}
}
