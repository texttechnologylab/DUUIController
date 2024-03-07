import { API_URL } from '$env/static/private'
import type { PageServerLoad } from './$types'

export const load: PageServerLoad = async ({ url, cookies, fetch }) => {
	const code = url.searchParams.get('code')

	const response = await fetch(`${API_URL}/users/auth/dropbox?code=${code}`, {
		method: 'PUT',
		headers: {
			Authorization: cookies.get('session') || ''
		}
	})

	return {
		success: response.ok
	}
}
