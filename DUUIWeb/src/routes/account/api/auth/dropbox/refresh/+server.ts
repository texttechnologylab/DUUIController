import { API_URL } from '$lib/config'
import { error } from '@sveltejs/kit'
import { DropboxAuth } from 'dropbox'

const dbxAuth = new DropboxAuth({
	clientId: 'l2nw2ign2z8h9hg',
	clientSecret: 'wqgejzv1xivdwki'
})

const redirectURI = `http://localhost:5173/user/auth/dropbox`

export async function POST({ request, cookies, locals }) {
	const data = await request.json()
	const user = locals.user

	const code = data.code
	if (!code) {
		error(404, 'Code not found in url')
	}

	const token = await dbxAuth.getAccessTokenFromCode(redirectURI, code)
	const dbx_access_token: string = token.result.access_token
	const dbx_refresh_token: string = token.result.refresh_token

	const response = await fetch(API_URL + '/users/' + user.id, {
		method: 'PUT',
		mode: 'cors',
		body: JSON.stringify({
			dbx_access_token: dbx_access_token,
			dbx_refresh_token: dbx_refresh_token
		}),
		headers: {
			session: cookies.get('session') || ''
		}
	})

	return response
}
