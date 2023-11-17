import { API_URL } from '$lib/config'
import type { DUUIPipeline } from '$lib/duui/pipeline'

export async function POST({ request, cookies }) {
	const data: DUUIPipeline = await request.json()

	const response = await fetch(`${API_URL}/pipelines/${data.oid}/start`, {
		method: 'PUT',
		mode: 'cors',
		body: JSON.stringify(data),
		headers: {
			authorization: cookies.get('session') || ''
		}
	})

	return response
}

export async function PUT({ request, cookies }) {
	const data: DUUIPipeline = await request.json()

	const response = await fetch(`${API_URL}/pipelines/${data.oid}/stop`, {
		method: 'PUT',
		mode: 'cors',
		body: JSON.stringify(data),
		headers: {
			authorization: cookies.get('session') || ''
		}
	})

	return response
}
