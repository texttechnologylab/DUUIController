import { API_URL } from '$env/static/private'
import { error } from '@sveltejs/kit'

export const PUT = async ({ request, locals, fetch }) => {
	const data = await request.json()
	const user = locals.user

	if (!user || user.role !== 'Admin') {
		return error(401, { message: 'Unauthorized' })
	}

	const response = await fetch(`${API_URL}/users/${data.oid}`, {
		method: 'PUT',
		body: JSON.stringify({ role: data.role })
	})

	return response
}

export const DELETE = async ({ cookies, locals, request }) => {
	const data = await request.json()
	const user = locals.user

	if (!user || user.role !== 'Admin') {
		return error(401, { message: 'Unauthorized' })
	}

	const response = await fetch(`${API_URL}/users/${data.oid}`, {
		method: 'DELETE',
		headers: {
			Authorization: cookies.get('session') || ''
		}
	})

	return response
}
