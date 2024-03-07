import { API_URL } from '$env/static/private'
import type { DUUIComponent } from '$lib/duui/component'
import type { PageServerLoad } from './$types'

export const load: PageServerLoad = async ({ cookies, fetch }) => {
	const fetchComponentTemplates = async (): Promise<{ components: DUUIComponent[] }> => {
		const response = await fetch(`${API_URL}/components`, {
			method: 'GET',

			headers: {
				Authorization: cookies.get('session') || ''
			}
		})

		if (response.ok) {
			return await response.json()
		}

		return { components: [] }
	}

	const fetchPipelineTemplates = async () => {
		const response = await fetch(
			`${API_URL}/pipelines?limit=25&sort=times_used&order=-1&templates=true`,
			{
				method: 'GET',

				headers: {
					Authorization: cookies.get('session') || ''
				}
			}
		)
		if (response.ok) {
			return await response.json()
		}

		return { pipelines: [] }
	}

	return {
		templateComponents: (await fetchComponentTemplates()).components,
		templatePipelines: (await fetchPipelineTemplates()).pipelines
	}
}
