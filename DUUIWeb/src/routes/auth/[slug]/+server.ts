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

	const response = await fetch(`${API_URL}/users/auth/login/${email}?key=${SERVER_API_KEY}`, {
		method: 'GET',
		mode: 'cors'
	})

	if (!response.ok) {
		return fail(400, {
			error: 'Unauthorized'
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
			error: 'IDK'
		})
	}

	const update = await fetch(`${API_URL}/users/${credentials.oid}?key=${SERVER_API_KEY}`, {
		method: 'PUT',
		mode: 'cors',
		body: JSON.stringify({
			session: crypto.randomUUID()
		})
	})

	const { user } = await update.json()

	return user
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

	const response = await fetch(`${API_URL}/users/auth/login/${email}?key=${SERVER_API_KEY}`, {
		method: 'GET',
		mode: 'cors'
	})

	if (response.status !== 404) {
		throw redirect(302, '/account/login?register=true&message=This email address is invalid')
	}

	if (password1 !== password2) {
		return fail(422, {
			error: 'Passwords do not match'
		})
	}

	const encryptedPassword = await bcrypt.hash(password1.toString(), 10)
	const session = crypto.randomUUID()

	const postResponse = await fetch(`${API_URL}/users?key=${SERVER_API_KEY}`, {
		method: 'POST',
		mode: 'cors',
		body: JSON.stringify({
			email: email,
			password: encryptedPassword,
			session: session,
			role: 'user'
		})
	})

	const { user } = await postResponse.json()

	return user
}

export const POST: RequestHandler = async (event) => {
	const { cookies } = event
	const { slug } = event.params

	let user

	try {
		switch (slug) {
			case 'logout':
				cookies.delete('session')
				return json({ message: 'Logout complete' })
			case 'login':
				user = await login(event)
				break
			case 'register':
				user = await register(event)
				break
			default:
				error(404, 'Unknown endpointSSSS')
				break
		}
	} catch (exception) {
		error(503, 'Could not communicate with database.')
	}

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
