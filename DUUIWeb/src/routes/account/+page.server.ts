import { API_URL } from '$env/static/private'
import { handleLoginRedirect } from '$lib/utils'
import { fail, redirect } from '@sveltejs/kit'
import { DropboxAuth } from 'dropbox'
import type { PageServerLoad } from './$types'

export const load: PageServerLoad = async ({ locals, cookies, url }) => {
	if (!locals.user) {
		redirect(302, handleLoginRedirect(url))
	}

	const response = await fetch(`${API_URL}/users/auth/dropbox`, {
		method: 'GET'
	})

	let dropbBoxURL: String = new String('')

	try {
		const credentials: {
			key: string
			secret: string
			url: string
		} = await response.json()

		const dbxAuth = new DropboxAuth({
			clientId: credentials.key,
			clientSecret: credentials.secret
		})

		dropbBoxURL = await dbxAuth.getAuthenticationUrl(
			credentials.url,
			cookies.get('session') || '',
			'code',
			'offline',
			undefined,
			undefined,
			false
		)
	} catch (error) {}

	const fetchProfile = async () => {
		const response = await fetch(`${API_URL}/users/${locals.user?.oid}`, {
			method: 'GET',
		})

		if (!response.ok) {
			return fail(response.status, { messgage: response.statusText })
		}
		return await response.json()
	}

	return {
		dropbBoxURL: dropbBoxURL,
		registered: (cookies.get('just_registered') || 'false') === 'true',
		user: (await fetchProfile()).user,
		theme: +(cookies.get('theme') || '0')
	}
}
