import { API_URL } from '$lib/config'
import { error } from '@sveltejs/kit'
import { DropboxAuth, DropboxResponse } from 'dropbox'

const dbxAuth = new DropboxAuth({
	clientId: 'l2nw2ign2z8h9hg',
	clientSecret: 'wqgejzv1xivdwki'
})

const redirectURI = `http://localhost:5173/account/auth/dropbox`

export async function POST({ request, cookies, locals }) {
	const data = await request.json()
	const user = locals.user

	const code = data.code
	if (!code) {
		throw error(404, 'Code not found in url')
	}

	const token: DropboxResponse<object> = await dbxAuth.getAccessTokenFromCode(redirectURI, code)

	const dbx_access_token: string = token.result.access_token
	const dbx_refresh_token: string = token.result.refresh_token

	const response = await fetch(`${API_URL}/users/${user.oid}`, {
		method: 'PUT',
		mode: 'cors',
		body: JSON.stringify({
			dbx_access_token: dbx_access_token,
			dbx_refresh_token: dbx_refresh_token
		}),
		headers: {
			authorization: cookies.get('session') || ''
		}
	})

	return response
}

export async function DELETE({ request, cookies, locals }) {
	const user = locals.user

	const response = await fetch(`${API_URL}/users/${user.oid}`, {
		method: 'PUT',
		mode: 'cors',
		body: JSON.stringify({
			dbx_access_token: null,
			dbx_refresh_token: null
		}),
		headers: {
			authorization: cookies.get('session') || ''
		}
	})

	return response
}
