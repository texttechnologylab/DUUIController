import type { DUUIPipelineComponent } from '$lib/data'
import type { PageServerLoad } from './$types'

export const load: PageServerLoad = async () => {
	const loadComponentTemplates = async (): Promise<{ components: DUUIPipelineComponent[] }> => {
		const result = await fetch('http://127.0.0.1:2605/components', {
			method: 'GET',
			mode: 'cors'
		})
		return await result.json()
	}

	return {
		templates: (await loadComponentTemplates()).components
	}
}
