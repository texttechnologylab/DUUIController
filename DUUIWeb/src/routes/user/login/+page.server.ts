import { fail, redirect } from '@sveltejs/kit'
import type { Actions, PageServerLoad } from './$types'
import bcrypt from 'bcrypt'
import { API_URL } from '$lib/config'

export const load: PageServerLoad = async ({ locals }) => {
	return {
		user: locals.user
	}
}

export const actions: Actions = {
	async login({ cookies, request }) {
		const data = await request.formData()
		const email = data.get('email')
		const password = data.get('password')
		
		if (!email || !password) {
			return fail(400, {
				error: 'Invalid credentials.'
			})
		}

		const response = await fetch(API_URL + '/users/' + email, {
			method: 'GET',
			mode: 'cors'
		})

		const user = await response.json()
		if (user['message'] === 'User not found') {
			return fail(400, {
				error: 'Invalid credentials.'
			})
		}

		if (!(await bcrypt.compare(password.toString(), user.password))) {
			return fail(400, {
				error: 'Invalid credentials.'
			})
		}

		const userUpdate = await fetch(API_URL + '/users', {
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

		throw redirect(302, '/pipelines')
	},

	async logout({ cookies, locals }) {
		cookies.set('session', '', {
			path: '/',
			httpOnly: true,
			sameSite: 'strict',
			secure: process.env.NODE_ENV === 'production',
			expires: new Date(0)
		})
		throw redirect(302, '/user/login')
	}
}
