import { API_URL } from '$env/static/private'
import type { PageServerLoad } from './$types'

export const load: PageServerLoad = async ({ url, cookies, fetch }) => {
	const code = url.searchParams.get('code')
	// const access_token = url.searchParams.get('access_token')
	// const expires_in = url.searchParams.get('expires_in')
	console.log(url)
	// const response = await fetch(`${API_URL}/users/auth/?code=${code}`, {
	// 	method: 'PUT',
	// 	headers: {
	// 		Authorization: cookies.get('session') || ''
	// 	}
	// })

	return {
		success: true
	}
}
