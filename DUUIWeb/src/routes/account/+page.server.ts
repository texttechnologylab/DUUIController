import {
	API_URL,
	API_KEY,
	DROPBOX_CLIENT_ID,
	DROPBOX_CLIENT_SECRET,
	DROPBOX_REDIRECT
} from '$env/static/private'
import { error, fail, redirect } from '@sveltejs/kit'
import { DropboxAuth } from 'dropbox'
import type { Actions, PageServerLoad } from './$types'
import { handleLoginRedirect } from '$lib/utils'

const dbxAuth = new DropboxAuth({
	clientId: DROPBOX_CLIENT_ID,
	clientSecret: DROPBOX_CLIENT_SECRET
})

const redirectURI = DROPBOX_REDIRECT

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
				Authorization: API_KEY
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
			error(401, 'Unauthorized')
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
