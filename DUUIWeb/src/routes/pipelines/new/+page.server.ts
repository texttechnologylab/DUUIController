import { API_URL } from '$lib/config'
import type { DUUIPipelineComponent } from '$lib/data'
import type { PageServerLoad } from './$types'

export const load: PageServerLoad = async () => {
	const loadComponentTemplates = async (): Promise<{ components: DUUIPipelineComponent[] }> => {
		const result = await fetch(API_URL + '/components', {
			method: 'GET',
			mode: 'cors'
		})
		return await result.json()
	}

	return {
		templates: (await loadComponentTemplates()).components
	}
}
