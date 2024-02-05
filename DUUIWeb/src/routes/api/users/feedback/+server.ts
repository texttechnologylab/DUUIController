import { API_URL } from '$lib/config'
import { error } from '@sveltejs/kit'

export const POST = async ({ request, cookies, locals }) => {
	const user = locals.user

	if (!user) {
		return error(401, { message: 'Unauthorized' })
	}

	const feedback: FormData = await request.formData()

	const response = await fetch(`${API_URL}/users/${user.oid}/feedback`, {
		method: 'POST',
		mode: 'cors',
		body: JSON.stringify(Object.fromEntries(feedback.entries())),
		headers: {
			Authorization: cookies.get('session') || ''
		}
	})

	return response
}
