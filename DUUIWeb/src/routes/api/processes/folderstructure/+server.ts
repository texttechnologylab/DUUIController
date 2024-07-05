import { API_URL } from '$env/static/private'
import type { DUUIDocument } from '$lib/duui/io'
import { error, json, type RequestHandler } from '@sveltejs/kit'

/**
 * Sends a get request to the backend to retrieve the folder structure of a database.
 */
export const POST: RequestHandler = async ({ request, cookies, fetch }) => {
	const data = await request.json()

	const response = await fetch(`${API_URL}/processes/folderstructure/${data.provider}/${data.user}`, {
		method: 'GET',
		headers: {
			Authorization: cookies.get('session') || ''
		}
	})

	if (response.ok) {
		const json = await response.json()
		return json
	}

	return {}
}

