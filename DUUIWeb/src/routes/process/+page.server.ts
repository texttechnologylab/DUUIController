import { DropboxAuth } from 'dropbox'
import type { PageServerLoad } from './$types'
import { SERVER_API_KEY } from '$env/static/private'
import { API_URL } from '$lib/config'
import { fail } from '@sveltejs/kit'

const dbxAuth = new DropboxAuth({
	clientId: 'l2nw2ign2z8h9hg',
	clientSecret: 'wqgejzv1xivdwki'
})

const redirectURI = `http://localhost:5173/account/auth/dropbox`

export const load: PageServerLoad = async ({ locals, cookies, url }) => {
	const getAuthURL = async () => {
		const response = await dbxAuth.getAuthenticationUrl(
			redirectURI,
			cookies.get('session') || '',
			'code',
			'offline',
			undefined,
			undefined,
			false
		)
		return response
	}

	const fetchProfile = async () => {
		const response = await fetch(`${API_URL}/users/${locals.user?.oid}?key=${SERVER_API_KEY}`, {
			method: 'GET',
			mode: 'cors'
		})

		if (!response.ok) {
			return fail(response.status, { messgage: response.statusText })
		}
		return await response.json()
	}

	return {
		dropbBoxURL: getAuthURL(),
		user: (await fetchProfile()).user
	}
}
