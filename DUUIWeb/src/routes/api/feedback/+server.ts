import { API_URL } from '$env/static/private'

export async function POST({ request, cookies }) {
	const data = await request.json()
	try {
		delete data['language']
		delete data['step']
	} catch (err) {}

	const response = await fetch(`${API_URL}/feedback`, {
		method: 'POST',
		body: JSON.stringify(data),
		headers: {
			Authorization: cookies.get('session') || ''
		}
	})

	return response
}
