import { fail, redirect } from '@sveltejs/kit'
import type { Actions, PageServerLoad } from './$types'
import bcrypt from 'bcrypt'
import { API_URL } from '$lib/config'
import { storage } from '$lib/store'

export const load: PageServerLoad = async ({ locals, url }) => {
	if (url.searchParams.get('logout') === 'true') {
		storage.set({
			session: '',
			user: {
				oid: '',
				authorization: '',
				email: '',
				role: 'user',
				preferences: {},
				connections: {}
			}
		})
	}
	
	return {
		user: locals.user
	}
}

export const actions: Actions = {
	async login({ cookies, request, url }) {
		const data = await request.formData()
		const email = data.get('email')
		const password = data.get('password')

		if (!email || !password) {
			return fail(400, {
				error: 'Invalid credentials.'
			})
		}

		const response = await fetch(`${API_URL}/users/${email}`, {
			method: 'GET',
			mode: 'cors'
		})

		const user = await response.json()
		if (user.status === 404) {
			return fail(400, {
				error: 'Invalid credentials.'
			})
		}

		if (!(await bcrypt.compare(password.toString(), user.password))) {
			return fail(400, {
				error: 'Invalid credentials.'
			})
		}

		const userUpdate = await fetch(`${API_URL}/users`, {
			method: 'PUT',
			mode: 'cors',
			body: JSON.stringify({
				email: email.toString(),
				session: crypto.randomUUID()
			})
		})

		const session = await userUpdate.json()

		cookies.set('session', session.session, {
			path: '/',
			httpOnly: true,
			sameSite: 'strict',
			secure: process.env.NODE_ENV === 'production',
			maxAge: 60 * 60 * 24 * 30
		})

		const redirectURL = data.get('redirect')?.toString() || '/account/user/profile'

		throw redirect(302, redirectURL)
	},

	async register({ request, cookies, locals }) {
		const data = await request.formData()
		const email = data.get('email')

		const response = await fetch(`${API_URL}/users/${email}`, {
			method: 'GET',
			mode: 'cors'
		})

		const user = await response.json()

		if (user['message'] !== 'User not found') {
			throw redirect(302, '/account/auth/login?register=true&message=This email address is invalid')
		}

		const password1 = data.get('password1')
		const password2 = data.get('password2')

		if (password1 !== password2) {
			return fail(422, {
				error: 'Passwords do not match'
			})
		}

		if (password1 === null) {
			return fail(422, {
				error: 'No password provided.'
			})
		}

		const encryptedPassword = await bcrypt.hash(password1.toString(), 10)

		const session = crypto.randomUUID()

		const created = await fetch(`${API_URL}/users`, {
			method: 'POST',
			mode: 'cors',
			body: JSON.stringify({
				email: email,
				password: encryptedPassword,
				session: session,
				role: 'user'
			})
		})

		if (created.ok) {
			cookies.set('session', session, {
				path: '/',
				httpOnly: true,
				sameSite: 'strict',
				secure: process.env.NODE_ENV === 'production',
				maxAge: 60 * 60 * 24 * 30
			})

			throw redirect(302, '/account/user/profile')
		}
		throw redirect(303, '/account/user/login')
	}
}
