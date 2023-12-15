import { API_URL } from '$lib/config'
import { type Actions, error } from '@sveltejs/kit'
import { DropboxAuth, DropboxResponse } from 'dropbox'
import type { PageServerLoad } from './$types'

const dbxAuth = new DropboxAuth({
	clientId: 'l2nw2ign2z8h9hg',
	clientSecret: 'wqgejzv1xivdwki'
})

const redirectURI = `http://localhost:5173/account/auth/dropbox`

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
	if (code !== null) {
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

	return {
		dropbBoxURL: getAuthURL()
	}
}
