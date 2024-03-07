import { API_URL } from '$env/static/private'
import { createSession } from '$lib/utils'
import { error, fail, json, type RequestEvent } from '@sveltejs/kit'
import bcrypt from 'bcrypt'
import type { RequestHandler, RouteParams } from './$types'

/**
 * Attempt to login the user by veryfing the entered credentaisl at /auth/login.
 * @returns
 */
const login = async (event: RequestEvent<RouteParams, '/auth/[slug]'>) => {
	const data = await event.request.json()

	const email = data.email
	const password = data.password

	if (!email || !password) {
		return fail(400, {
			error: 'Invalid credentials.'
		})
	}

	const response = await fetch(`${API_URL}/users/auth/login/${email}`, {
		method: 'GET'
	})

	if (!response.ok) {
		return fail(400, {
			error: 'Unknown Email address'
		})
	}

	const { credentials } = await response.json()

	try {
		if (!(await bcrypt.compare(password.toString(), credentials.password))) {
			return fail(400, {
				error: 'Invalid credentials.'
			})
		}
	} catch (err) {
		return fail(400, {
			error: 'Error during Login'
		})
	}

	const update = await fetch(`${API_URL}/users/${credentials.oid}`, {
		method: 'PUT',

		body: JSON.stringify({
			session: crypto.randomUUID()
		})
	})

	const { user } = await update.json()

	return json({ user: user })
}

/**
 * Attempt to register a user by sending a request to the backend and checking if the email address is available.
 */
const register = async (event: RequestEvent<RouteParams, '/auth/[slug]'>) => {
	const data = await event.request.json()

	const email = data.email
	const password1 = data.password1
	const password2 = data.password2

	if (!email || !password1 || !password2) {
		return fail(400, {
			error: 'Invalid credentials.'
		})
	}

	const response = await fetch(`${API_URL}/users/auth/login/${email}`, {
		method: 'GET'
	})

	if (response.status === 404) {
		if (password1 !== password2) {
			return fail(422, {
				error: 'Passwords do not match'
			})
		}

		const encryptedPassword = await bcrypt.hash(password1.toString(), 10)
		const session = crypto.randomUUID()

		const postResponse = await fetch(`${API_URL}/users`, {
			method: 'POST',

			body: JSON.stringify({
				email: email,
				password: encryptedPassword,
				session: session,
				role: 'User'
			})
		})

		const { user } = await postResponse.json()
		return json({ user: user })
	}

	if (response.status === 401) {
		return fail(302, {
			error: 'Something went wrong during registration. Please try again later.'
		})
	}

	return fail(302, {
		error:
			'This email address is already registered. If you forgot your password, you can reset it.'
	})
}

/**
 * Handle different form requests from the /auth paths.
 */
export const POST: RequestHandler = async (event) => {
	const { cookies } = event
	const { slug } = event.params

	let authenticationResult

	try {
		switch (slug) {
			case 'logout':
				cookies.delete('session', { path: '/' })
				return json({ message: 'Logout complete' })
			case 'login':
				authenticationResult = await login(event)
				break
			case 'register':
				authenticationResult = await register(event)
				break
			case 'recover':
				return json('Success')
			default:
				error(404, 'Unknown endpoint')
		}
	} catch (exception) {
		error(503, 'Could not communicate with database.')
	}

	if (authenticationResult.status !== 200) {
		return json(authenticationResult.data.error, {
			status: authenticationResult.status,
			statusText: authenticationResult.data.error
		})
	}

	const { user } = await authenticationResult.json()

	event.locals.user = user
	createSession(cookies, user.session)
	return json({ user: user })
}
