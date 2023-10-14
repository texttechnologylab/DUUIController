import { API_URL } from "$lib/config"

export async function POST({ request, cookies }) {
	const data = await request.json()
	const response = await fetch(API_URL + '/pipelines', {
		method: 'POST',
		mode: 'cors',
		body: JSON.stringify(data),
		headers: {
			session: cookies.get('session') || ''
		}
	})

	return response
}
