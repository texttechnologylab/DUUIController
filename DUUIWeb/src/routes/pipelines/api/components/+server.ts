import { BASE_URL } from '$lib/data'

export async function GET() {
	const response = await fetch(BASE_URL + '/components', {
		method: 'GET',
		mode: 'cors'
	})

	return response
}
