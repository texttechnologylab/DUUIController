import { DROPBOX_CLIENT_ID, DROPBOX_CLIENT_SECRET, SERVER_API_KEY } from '$env/static/private'
import { API_URL } from '$lib/config'
import { fail, redirect } from '@sveltejs/kit'
import { DropboxAuth } from 'dropbox'
import type { Actions, PageServerLoad } from './$types'
import { handleLoginRedirect } from '$lib/utils'

const dbxAuth = new DropboxAuth({
	clientId: DROPBOX_CLIENT_ID,
	clientSecret: DROPBOX_CLIENT_SECRET
})

const redirectURI = `http://localhost:5173/account/dropbox`

export const load: PageServerLoad = async ({ locals, cookies, url }) => {
	if (!locals.user) {
		redirect(302, handleLoginRedirect(url))
	}

	const getDropboxAuthURL = async () => {
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
		dropbBoxURL: getDropboxAuthURL(),
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
