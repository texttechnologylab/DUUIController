import { redirect } from '@sveltejs/kit'
import type { LayoutServerLoad } from '../../$types'
import { handleLoginRedirect } from '$lib/utils'
import { API_URL } from '$lib/config'

export const load: LayoutServerLoad = async ({ locals, url, cookies }) => {
	if (!locals.user) {
		throw redirect(302, handleLoginRedirect(url))
	}

	return {
		user: locals.user,
		session: cookies.get('session')
	}

	const session = url.searchParams.get('state') || cookies.get('session')

	if (!locals.user && !session) {
		throw redirect(302, handleLoginRedirect(url))
	}

	const response = await fetch(`${API_URL}/users/auth/${session}`, {
		method: 'GET',
		mode: 'cors'
	})

	if (response.ok) {
		const user = await response.json()

		locals.user = {
			oid: user.oid,
			preferences: user.preferences,
			authorization: user.authorization,
			email: user.email,
			role: user.role,
			connections: user.connections
		}

		cookies.set('session', session, {
			path: '/',
			httpOnly: true,
			sameSite: 'strict',
			secure: process.env.NODE_ENV === 'production',
			maxAge: 60 * 60 * 24 * 30
		})

		throw redirect(302, '/account/user/connections')
	}
}
