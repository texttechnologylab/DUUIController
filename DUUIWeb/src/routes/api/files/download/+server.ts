import { API_URL } from '$env/static/private'

/**
 * Try to download a file by sending a request to the backend.
 */
export async function GET({ url, cookies, fetch }) {
	const provider = url.searchParams.get('provider')
	const path = url.searchParams.get('path')

	const response = await fetch(`${API_URL}/files?provider=${provider}&path=${path}`, {
		method: 'GET',

		headers: {
			Authorization: cookies.get('session') || ''
		}
	})

	return response
}
