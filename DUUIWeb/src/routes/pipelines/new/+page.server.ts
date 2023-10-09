import { BASE_URL, type DUUIPipelineComponent } from '$lib/data'
import type { Actions, PageServerLoad } from './$types'

export const load: PageServerLoad = async () => {
	const loadComponentTemplates = async (): Promise<{ components: DUUIPipelineComponent[] }> => {
		const result = await fetch(BASE_URL + '/components', {
			method: 'GET',
			mode: 'cors'
		})
		return await result.json()
	}

	return {
		templates: (await loadComponentTemplates()).components
	}
}

