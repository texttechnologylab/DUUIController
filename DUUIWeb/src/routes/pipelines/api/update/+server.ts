import { BASE_URL } from "$lib/data"

export async function POST({ request, cookies }) {
	const data = await request.json()
	const response = await fetch(BASE_URL + '/pipelines/' + data.id, {
		method: 'PUT',
		mode: 'cors',
		body: JSON.stringify(data),
		headers: {
			session: cookies.get('session') || ''
		}
	})

	return response
}
