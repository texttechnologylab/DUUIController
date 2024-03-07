import { API_URL } from '$env/static/private'
import { json } from '@sveltejs/kit'

/**
 * Try to upload a file by sending a request to the backend providing one or more files as formData.
 */
export async function POST({ request, cookies, url, fetch }) {
	const formData = await request.formData()
	const store: boolean = (url.searchParams.get('store') || 'false') === 'true'
	const provider: string = url.searchParams.get('provider') || ''
	const path: string = url.searchParams.get('path') || ''

	const response = await fetch(
		`${API_URL}/files?store=${store}&provider=${provider}&path=${path}`,
		{
			method: 'POST',
			body: formData,
			headers: {
				Authorization: cookies.get('session') || ''
			}
		}
	)

	if (response.ok) {
		return json(await response.json(), {
			headers: {
				'Content-Type': 'application/json'
			},
			status: 200
		})
	}

	return response
}
