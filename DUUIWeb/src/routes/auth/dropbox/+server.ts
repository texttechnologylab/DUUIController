import { DROPBOX_CLIENT_ID, DROPBOX_CLIENT_SECRET, SERVER_API_KEY } from '$env/static/private'
import { API_URL } from '$lib/config'
import { error } from '@sveltejs/kit'
import { DropboxAuth, DropboxResponse } from 'dropbox'

const dbxAuth = new DropboxAuth({
	clientId: DROPBOX_CLIENT_ID,
	clientSecret: DROPBOX_CLIENT_SECRET
})

const redirectURI = `http://localhost:5173/account`

export async function POST({ request, locals }) {
	const data = await request.json()
	const user = locals.user

	const code = data.code
	if (!code) {
		error(404, 'Code not found in url');
	}

	const token: DropboxResponse<object> = await dbxAuth.getAccessTokenFromCode(redirectURI, code)

	const dbx_access_token: string = token.result.access_token
	const dbx_refresh_token: string = token.result.refresh_token

	const response = await fetch(`${API_URL}/users/${user.oid}`, {
		method: 'PUT',
		mode: 'cors',
		body: JSON.stringify({
			dropbox: {
				access_token: dbx_access_token,
				refresh_token: dbx_refresh_token
			}
		}),
		headers: {
			Authorization: SERVER_API_KEY
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
			dropbox: {
				access_token: null,
				refresh_token: null
			}
		}),
		headers: {
			Authorization: SERVER_API_KEY
		}
	})

	return response
}
