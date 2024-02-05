import { API_URL } from '$lib/config'

export async function GET({ url, cookies }) {
	const provider = url.searchParams.get('provider')
	const path = url.searchParams.get('path')

	const response = await fetch(`${API_URL}/files?provider=${provider}&path=${path}`, {
		method: 'GET',
		mode: 'cors',
		headers: {
			Authorization: cookies.get('session') || ''
		}
	})


	return response
}
