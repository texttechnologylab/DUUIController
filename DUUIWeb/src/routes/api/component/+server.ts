import { API_URL } from '$lib/config'

export async function POST({ request, cookies }) {
	const { component } = await request.json()

	const response = await fetch(`${API_URL}/components`, {
		method: 'POST',
		mode: 'cors',
		body: JSON.stringify(component),
		headers: {
			Authorization: cookies.get('session') || ''
		}
	})

	return response
}
