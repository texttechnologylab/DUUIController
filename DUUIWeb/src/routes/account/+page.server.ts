import { DROPBOX_CLIENT_ID, DROPBOX_CLIENT_SECRET, SERVER_API_KEY } from '$env/static/private'
import { API_URL } from '$lib/config'
import { fail, redirect } from '@sveltejs/kit'
import type { Actions, PageServerLoad } from './$types'
import { DropboxAuth, type DropboxResponse } from 'dropbox'

const dbxAuth = new DropboxAuth({
	clientId: DROPBOX_CLIENT_ID,
	clientSecret: DROPBOX_CLIENT_SECRET
})

const redirectURI = `http://localhost:5173/account`

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
		} catch (err) {
			console.log(err)
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

export const actions: Actions = {
	async deleteAccount({ locals, cookies }) {
		const user = locals.user
		if (!user) {
			return fail(400, { message: 'Not logged in' })
		}

		await fetch(`${API_URL}/users/${user.oid}`, {
			method: 'DELETE',
			mode: 'cors',
			headers: {
				Authorization: cookies.get('session') || ''
			}
		})

		cookies.set('session', '', {
			path: '/',
			httpOnly: true,
			sameSite: 'strict',
			secure: process.env.NODE_ENV === 'production',
			expires: new Date(0)
		})
	}
}
