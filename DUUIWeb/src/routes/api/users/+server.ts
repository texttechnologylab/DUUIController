import { API_URL } from '$env/static/private'
import { error } from '@sveltejs/kit'

export const PUT = async ({ request, locals }) => {
	const user = locals.user

	if (!user) {
		return error(401, { message: 'Unauthorized' })
	}

	const response = await fetch(`${API_URL}/users/${user.oid}`, {
		method: 'PUT',
		mode: 'cors',
		body: JSON.stringify(await request.json())
	})

	return response
}

export const DELETE = async ({ cookies, locals }) => {
	const user = locals.user
	if (!user) {
		error(401, 'Unauthorized')
	}

	await fetch(`${API_URL}/users/${user.oid}`, {
		method: 'DELETE',
		mode: 'cors',
		headers: {
			Authorization: cookies.get('session') || ''
		}
	})

	cookies.delete('session', { path: '/' })
}
