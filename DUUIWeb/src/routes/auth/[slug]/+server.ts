import { error, fail, json, redirect, type RequestEvent } from '@sveltejs/kit'
import type { RequestHandler, RouteParams } from './$types'
import { SERVER_API_KEY } from '$env/static/private'
import { API_URL } from '$lib/config'
import bcrypt from 'bcrypt'

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
		method: 'GET',
		mode: 'cors',
		headers: {
			Authorization: SERVER_API_KEY
		}
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
		mode: 'cors',
		body: JSON.stringify({
			session: crypto.randomUUID()
		}),
		headers: {
			Authorization: SERVER_API_KEY
		}
	})

	const { user } = await update.json()
	return json({ user: user })
}

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
		method: 'GET',
		mode: 'cors',
		headers: {
			Authorization: SERVER_API_KEY
		}
	})

	if (response.status !== 404) {
		throw fail(302, { error: '/account/register?message=This email address is invalid' })
	}

	if (password1 !== password2) {
		return fail(422, {
			error: 'Passwords do not match'
		})
	}

	const encryptedPassword = await bcrypt.hash(password1.toString(), 10)
	const session = crypto.randomUUID()

	const postResponse = await fetch(`${API_URL}/users`, {
		method: 'POST',
		mode: 'cors',
		headers: {
			Authorization: SERVER_API_KEY
		},
		body: JSON.stringify({
			email: email,
			password: encryptedPassword,
			session: session,
			role: 'user'
		})
	})

	const { user } = await postResponse.json()
	return json({ user: user })
}

export const POST: RequestHandler = async (event) => {
	const { cookies } = event
	const { slug } = event.params

	let authenticationResult

	try {
		switch (slug) {
			case 'logout':
				cookies.delete('session')
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
				break
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
	cookies.set('session', user.session, {
		path: '/',
		httpOnly: true,
		sameSite: 'strict',
		secure: process.env.NODE_ENV === 'production',
		maxAge: 60 * 60 * 24 * 30
	})

	return json({ user: user })
}
