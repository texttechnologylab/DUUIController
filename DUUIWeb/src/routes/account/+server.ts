import { API_URL } from '$lib/config'
import { error } from '@sveltejs/kit'

export async function PUT({ request, locals }) {
	const user = locals.user
	if (!user) {
		error(401, { message: 'Unauthorized' })
	}

	const response = await fetch(`${API_URL}/users/${user.oid}`, {
		method: 'PUT',
		mode: 'cors',
		body: JSON.stringify(await request.json())
	})

	return response
}
