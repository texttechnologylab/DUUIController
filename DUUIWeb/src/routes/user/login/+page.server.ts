import { fail, redirect } from '@sveltejs/kit'
import type { Actions, PageServerLoad } from './$types'
import bcrypt from 'bcrypt'

export const load: PageServerLoad = async ({ locals }) => {
	return {
		user: locals.user
	}
	if (locals.user) {
		throw redirect(302, '/user/logout')
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

		const response = await fetch('http://127.0.0.1:2605/users/' + email, {
			method: 'GET',
			mode: 'cors'
		})

		const user = await response.json()

		if (Object.keys(user).length === 0) {
			return fail(400, {
				error: 'Invalid credentials.'
			})
		}

		if (!(await bcrypt.compare(password.toString(), user.password))) {
			return fail(400, {
				error: 'Invalid credentials.'
			})
		}

		const userUpdate = await fetch('http://127.0.0.1:2605/users', {
			method: 'PUT',
			mode: 'cors',
			body: JSON.stringify({
				email: email.toString(),
				userAuthToken: crypto.randomUUID()
			})
		})

		const userToken = await userUpdate.json()

		cookies.set('session', userToken.userAuthToken, {
			path: '/',
			httpOnly: true,
			sameSite: 'strict',
			secure: process.env.NODE_ENV === 'production',
			maxAge: 60 * 60 * 24 * 30
		})
		throw redirect(302, '/pipelines')
	},

	async logout({ cookies }) {
		cookies.set('session', '', {
			path: '/',
			expires: new Date(0)
		})
		throw redirect(302, '/user/login')
	}
}
