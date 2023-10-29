import { API_URL } from '$lib/config'
import type { DUUIPipeline } from '$lib/duui/pipeline'

export async function DELETE({ request, cookies }) {
	const data: DUUIPipeline = await request.json()

	const response = await fetch(`${API_URL}/pipelines/${data.id}/stop`, {
		method: 'PUT',
		mode: 'cors',
		body: JSON.stringify(data),
		headers: {
			session: cookies.get('session') || ''
		}
	})

	return response
}
