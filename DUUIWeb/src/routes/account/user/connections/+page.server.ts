import { API_URL } from '$lib/config'
import { DropboxAuth, DropboxResponse } from 'dropbox'
import type { PageServerLoad } from './$types'

import { fail } from '@sveltejs/kit'
import { DROPBOX_CLIENT_ID, DROPBOX_CLIENT_SECRET, SERVER_API_KEY } from '$env/static/private'

const dbxAuth = new DropboxAuth({
	clientId: DROPBOX_CLIENT_ID,
	clientSecret: DROPBOX_CLIENT_SECRET
})

const redirectURI = `http://localhost:5173/account`

const connect = async (code: string, oid: string, session: string) => {
	const token: DropboxResponse<object> = await dbxAuth.getAccessTokenFromCode(redirectURI, code)

	const dbx_access_token: string = token.result.access_token
	const dbx_refresh_token: string = token.result.refresh_token

	const response = await fetch(`${API_URL}/users/${oid}`, {
		method: 'PUT',
		mode: 'cors',
		body: JSON.stringify({
			dropbox: {
				access_token: dbx_access_token,
				refresh_token: dbx_refresh_token
			}
		}),
		headers: {
			authorization: session
		}
	})

	return response
}

export const load: PageServerLoad = async ({ url, cookies, locals }) => {
	const code = url.searchParams.get('code')
	if (code !== null && locals.user) {
		const update = await connect(code, locals.user.oid, cookies.get('session') || '')
		if (update.ok) {
			locals.user.connections.dropbox = true
		}
	}

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
		dropbBoxURL: getAuthURL(),
		user: (await fetchProfile()).user
	}
}
