import type { Handle } from '@sveltejs/kit'

export const handle: Handle = async ({ event, resolve }) => {
	const session = event.cookies.get('session')
	if (!session) {
		return await resolve(event)
	}
	
	let userResponse

	try {
		userResponse = await fetch('http://127.0.0.1:2605/users/auth/' + session, {
			method: 'GET',
			mode: 'cors'
		})
	} catch (e) {
		return await resolve(event)
	}

	const user = await userResponse.json()

	if (Object.keys(user).length !== 0) {
		event.locals.user = {
			id: user.id,
			email: user.email,
			role: user.role
		}
	}

	return await resolve(event)
}
