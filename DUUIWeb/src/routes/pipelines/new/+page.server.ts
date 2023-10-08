import type { DUUIPipelineComponent } from '$lib/data'
import type { Actions, PageServerLoad } from './$types'

export const load: PageServerLoad = async () => {
	const loadComponentTemplates = async (): Promise<{ components: DUUIPipelineComponent[] }> => {
		const result = await fetch('http://192.168.2.122:2605/components', {
			method: 'GET',
			mode: 'cors'
		})
		return await result.json()
	}

	return {
		templates: (await loadComponentTemplates()).components
	}
}

