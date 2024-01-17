import { DROPBOX_CLIENT_ID, DROPBOX_CLIENT_SECRET, SERVER_API_KEY } from '$env/static/private'
import { API_URL } from '$lib/config'
import { DropboxAuth, type DropboxResponse } from 'dropbox'
import type { PageServerLoad } from './$types'
import { goto } from '$app/navigation'
import { redirect } from '@sveltejs/kit'

const dbxAuth = new DropboxAuth({
	clientId: DROPBOX_CLIENT_ID,
	clientSecret: DROPBOX_CLIENT_SECRET
})

const redirectURI = `http://localhost:5173/account/dropbox`

const connect = async (code: string, user: User) => {
	const token: DropboxResponse<object> = await dbxAuth.getAccessTokenFromCode(redirectURI, code)

	const dbx_access_token: string = token.result.access_token
	const dbx_refresh_token: string = token.result.refresh_token

	const response = await fetch(`${API_URL}/users/${user?.oid}`, {
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

export const load: PageServerLoad = async ({ locals, url, cookies }) => {
	const code = url.searchParams.get('code')

	if (code !== null && locals.user) {
		try {
			await connect(code, locals.user)
		} catch (err) {}

		redirect(300, '/account')
	}
}
