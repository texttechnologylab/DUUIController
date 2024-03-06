import { API_URL } from '$env/static/private'
import type { DUUIPipeline } from '$lib/duui/pipeline'

export async function POST({ request, cookies, fetch }) {
	const data: DUUIPipeline = await request.json()

	const response = await fetch(`${API_URL}/pipelines/${data.oid}/start`, {
		method: 'POST',
		
		headers: {
			Authorization: cookies.get('session') || ''
		}
	})

	return response
}

export async function PUT({ request, cookies, fetch }) {
	const data: DUUIPipeline = await request.json()

	const response = await fetch(`${API_URL}/pipelines/${data.oid}/stop`, {
		method: 'PUT',
		
		headers: {
			Authorization: cookies.get('session') || ''
		}
	})

	return response
}
