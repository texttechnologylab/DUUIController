import { API_URL } from '$lib/config'

export async function POST({ request, cookies }) {
	const data = await request.json()

	const response = await fetch(API_URL + '/templates/pipelines', {
		method: 'POST',
		mode: 'cors',
		body: JSON.stringify(data),
		headers: {
			Authorization: cookies.get('session') || ''
		}
	})

	return response
}
