import { API_URL } from '$lib/config'
import type { Actions } from '@sveltejs/kit'

export const actions: Actions = {
	async deleteAccount({ locals, cookies }) {
		const user = locals.user

		await fetch(`${API_URL}/users/${user.oid}`, {
			method: 'DELETE',
			mode: 'cors',
			headers: {
				authorization: cookies.get('session') || ''
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
