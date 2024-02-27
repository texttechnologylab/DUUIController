import { API_URL } from '$env/static/private'
import { handleLoginRedirect } from '$lib/utils'
import { error, fail, redirect } from '@sveltejs/kit'
import { DropboxAuth } from 'dropbox'
import type { Actions, PageServerLoad } from './$types'

export const load: PageServerLoad = async ({ locals, cookies, url }) => {
	if (!locals.user) {
		redirect(302, handleLoginRedirect(url))
	}

	const response = await fetch(`${API_URL}/users/auth/dropbox`, {
		method: 'GET'
	})

	const credentials: {
		key: string
		secret: string
		url: string
	} = await response.json()

	const dbxAuth = new DropboxAuth({
		clientId: credentials.key,
		clientSecret: credentials.secret
	})

	const getDropboxAuthURL = async () => {
		const response = await dbxAuth.getAuthenticationUrl(
			credentials.url,
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
			mode: 'cors'
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
