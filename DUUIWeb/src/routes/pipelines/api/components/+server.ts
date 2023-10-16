import { API_URL } from '$lib/config'

export async function GET({ cookies }) {
	const response = await fetch(API_URL + '/components', {
		method: 'GET',
		mode: 'cors',
		headers: {
			session: cookies.get('session') || ''
		}
	})

	return response
}
