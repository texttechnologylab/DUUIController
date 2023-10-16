import { API_URL } from '$lib/config'
import type { DUUIPipeline } from '$lib/duui/pipeline'

export async function POST({ request, cookies }) {
	const data: DUUIPipeline = await request.json()

	data.components.forEach(async (component) => {
		if (component.settings.options['saveAsTemplate']) {
			await fetch(API_URL + '/components', {
				method: 'POST',
				mode: 'cors',
				body: JSON.stringify(component),
				headers: {
					session: cookies.get('session') || ''
				}
			})
		}
	})

	const response = await fetch(API_URL + '/pipelines', {
		method: 'POST',
		mode: 'cors',
		body: JSON.stringify(data),
		headers: {
			session: cookies.get('session') || ''
		}
	})

	return response
}
