import { fail, redirect } from '@sveltejs/kit'
import type { LayoutServerLoad } from '../../$types'
import { handleLoginRedirect } from '$lib/utils'
import { API_URL } from '$lib/config'

export const load: LayoutServerLoad = async ({ locals, url, cookies }) => {
	if (!locals.user) {
		throw redirect(302, handleLoginRedirect(url))
	}

	const fetchProfile = async () => {
		const response = await fetch(`${API_URL}/users/${locals.user?.oid}`, {
			method: 'GET',
			mode: 'cors'
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
