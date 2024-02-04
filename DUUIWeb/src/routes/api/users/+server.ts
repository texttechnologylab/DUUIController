import { API_URL, API_KEY } from '$env/static/private'
import { fail } from '@sveltejs/kit'

export const PUT = async ({ request, locals }) => {
	const user = locals.user

	if (!user) {
		return fail(401, { message: 'Unauthorized' })
	}

	const response = await fetch(`${API_URL}/users/${user.oid}`, {
		method: 'PUT',
		mode: 'cors',
		body: JSON.stringify(await request.json()),
		headers: {
			Authorization: API_KEY
		}
	})

	return response
}
