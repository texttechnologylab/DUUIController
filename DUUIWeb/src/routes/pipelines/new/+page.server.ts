import { API_URL } from '$lib/config'
import type { DUUIComponent } from '$lib/duui/component'
import type { PageServerLoad } from './$types'

export const load: PageServerLoad = async ({ cookies }) => {
	const fetchTemplates = async (): Promise<{ components: DUUIComponent[] }> => {
		const result = await fetch(API_URL + '/components', {
			method: 'GET',
			mode: 'cors',
			headers: {
				session: cookies.get('session') || ''
			}
		})
		return await result.json()
	}
	
	return {
		templates: (await fetchTemplates()).components
	}
}
