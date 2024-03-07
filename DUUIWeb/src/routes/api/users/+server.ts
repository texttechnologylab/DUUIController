import { API_URL } from '$env/static/private'
import { error } from '@sveltejs/kit'

/**
 * Sends a put request to the backend to update a user.
 */
export const PUT = async ({ request, locals, fetch }) => {
	const user = locals.user

	if (!user) {
		return error(401, { message: 'Unauthorized' })
	}

	const response = await fetch(`${API_URL}/users/${user.oid}`, {
		method: 'PUT',
		body: JSON.stringify(await request.json())
	})

	return response
}

/**
 * Sends a get request to the backend to delete a user.
 */
export const DELETE = async ({ cookies, locals }) => {
	const user = locals.user
	if (!user) {
		error(401, 'Unauthorized')
	}

	const response = await fetch(`${API_URL}/users/${user.oid}`, {
		method: 'DELETE',

		headers: {
			Authorization: cookies.get('session') || ''
		}
	})

	cookies.delete('session', { path: '/' })
	return response
}
