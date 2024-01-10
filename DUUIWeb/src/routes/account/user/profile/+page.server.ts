import { SERVER_API_KEY } from '$env/static/private'
import { API_URL } from '$lib/config'
import { fail } from '@sveltejs/kit'
import type { PageServerLoad } from './$types'

export const load: PageServerLoad = async ({ locals }) => {
	const fetchProfile = async () => {
		const response = await fetch(`${API_URL}/users/${locals.user?.oid}`, {
			method: 'GET',
			mode: 'cors',
			headers: {
				Authorization: SERVER_API_KEY
			}
		})

		if (!response.ok) {
			return fail(response.status, { messgage: response.statusText })
		}
		return await response.json()
	}

	return {
		user: (await fetchProfile()).user
	}
}
