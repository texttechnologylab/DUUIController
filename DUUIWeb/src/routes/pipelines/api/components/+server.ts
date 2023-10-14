import { API_URL } from '$lib/config'

export async function GET() {
	const response = await fetch(API_URL + '/components', {
		method: 'GET',
		mode: 'cors'
	})

	return response
}
