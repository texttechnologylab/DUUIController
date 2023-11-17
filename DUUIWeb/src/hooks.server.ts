import { API_URL } from '$lib/config'
import { storage } from '$lib/store'
import type { Handle } from '@sveltejs/kit'
import { get } from 'svelte/store'

export const handle: Handle = async ({ event, resolve }) => {
	let session = event.cookies.get('session')

	if (!session) {
		session = get(storage).session
	}

	if (!session) {
		return await resolve(event)
	}

	let userResponse

	try {
		userResponse = await fetch(`${API_URL}/users/auth/${session}`, {
			method: 'GET',
			mode: 'cors'
		})
	} catch (e) {
		return await resolve(event)
	}

	const user = await userResponse.json()

	if (userResponse.ok) {
		event.locals.user = {
			oid: user.oid,
			email: user.email || '',
			role: user.role,
			authorization: user.authorization,
			preferences: user.preferences,
			connections: user.connections
		}

		storage.set({
			session: session,
			user: event.locals.user
		})
	}

	return await resolve(event)
}
