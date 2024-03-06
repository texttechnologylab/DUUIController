import { API_URL } from '$env/static/private'
import { error } from '@sveltejs/kit'

export const POST = async ({ request, cookies, locals, fetch }) => {
	const user = locals.user

	if (!user) {
		return error(401, { message: 'Unauthorized' })
	}

	const feedback: FormData = await request.formData()

	const response = await fetch(`${API_URL}/users/${user.oid}/feedback`, {
		method: 'POST',

		body: JSON.stringify(Object.fromEntries(feedback.entries())),
		headers: {
			Authorization: cookies.get('session') || ''
		}
	})

	return response
}
