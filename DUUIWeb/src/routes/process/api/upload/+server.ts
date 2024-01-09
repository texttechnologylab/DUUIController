import { API_URL } from '$lib/config'

export async function POST({ request, cookies }) {
	const formData = await request.formData()

	const response = await fetch(`${API_URL}/processes/file`, {
		method: 'POST',
		mode: 'cors',
		body: formData,
		headers: {
			Authorization: cookies.get('session') || ''
		}
	})

	return response
}
