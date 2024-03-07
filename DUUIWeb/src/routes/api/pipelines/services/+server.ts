import { API_URL } from '$env/static/private'
import type { DUUIPipeline } from '$lib/duui/pipeline'

/**
 * Sends a post request to the backend to instantiate a pipeline.
 */
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

/**
 * Sends a put request to the backend to shutdown a pipeline.
 */
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
