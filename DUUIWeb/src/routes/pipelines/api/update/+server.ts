import { API_URL } from '$lib/config'
import type { RequestHandler } from '@sveltejs/kit'

export const GET: RequestHandler = async ({ url, cookies }) => {
	const id: string = url.searchParams.get('id') || ''

	const response = await fetch(`${API_URL}/pipelines/${id}`, {
		method: 'GET',
		headers: {
			Authorization: cookies.get('session') || ''
		}
	})

	return response
}
