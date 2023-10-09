import { BASE_URL } from '$lib/data'
import type { Handle } from '@sveltejs/kit'

export const handle: Handle = async ({ event, resolve }) => {
	const session = event.cookies.get('session')

	if (!session) {
		return await resolve(event)
	}

	let userResponse

	try {
		userResponse = await fetch(BASE_URL + '/users/auth/' + session, {
			method: 'GET',
			mode: 'cors'
		})
	} catch (e) {
		return await resolve(event)
	}

	const user = await userResponse.json()
	if (userResponse.status === 200) {
		event.locals.user = {
			id: user.id,
			email: user.email,
			role: user.role
		}
	}

	return await resolve(event)
}
