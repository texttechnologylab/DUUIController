import { API_URL } from '$lib/config'
import type { DUUIComponent } from '$lib/duui/component'
import type { DUUIPipeline } from '$lib/duui/pipeline'
import type { PageServerLoad } from './$types'

export const load: PageServerLoad = async ({ cookies }) => {
	const fetchComponentTemplates = async (): Promise<{ components: DUUIComponent[] }> => {
		const result = await fetch(API_URL + '/components', {
			method: 'GET',
			mode: 'cors',
			headers: {
				authorization: cookies.get('session') || ''
			}
		})
		return await result.json()
	}

	const fetchPipelineTemplates = async () => {
		const result = await fetch(API_URL + '/pipelines', {
			method: 'GET',
			mode: 'cors',
			headers: {
				authorization: cookies.get('session') || ''
			}
		})
		return await result.json()
	}

	const fetchPipelines = async (): Promise<{ pipelines: DUUIPipeline[] }> => {
		const result = await fetch(API_URL + '/pipelines/user/all', {
			method: 'GET',
			mode: 'cors',
			headers: {
				authorization: cookies.get('session') || ''
			}
		})
		return await result.json()
	}

	return {
		templateComponents: (await fetchComponentTemplates()).components,
		templatePipelines: (await fetchPipelineTemplates()).pipelines,
		userPipelines: (await fetchPipelineTemplates()).pipelines
	}
}
