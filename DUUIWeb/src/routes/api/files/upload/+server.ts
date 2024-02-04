import { API_URL } from '$env/static/private'

export async function POST({ request, cookies }) {
	const formData = await request.formData()

	const response = await fetch(`${API_URL}/processes/files`, {
		method: 'POST',
		mode: 'cors',
		body: formData,
		headers: {
			Authorization: cookies.get('session') || ''
		}
	})

	return response
}
