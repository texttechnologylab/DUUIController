import { API_URL } from '$lib/config'
import { randomBytes } from 'crypto'

const generatKey = (length: number = 64, format: BufferEncoding | undefined = 'base64') => {
	const buffer = randomBytes(length)
	return buffer.toString(format)
}

export async function GET({ request, cookies, locals }) {
	const user = locals.user
	if (!user) {
		return new Response(
			JSON.stringify({ message: 'You must be logged in to generate an API key.' }),
			{ status: 401 }
		)
	}

	const key = generatKey()

	const response = await fetch(`${API_URL}/users/authorization`, {
		method: 'PUT',
		mode: 'cors',
		body: JSON.stringify({
			key: key
		}),
		headers: {
			authorization: cookies.get('session') || ''
		}
	})

	if (response.ok) {
		return new Response(JSON.stringify({ authorization: key }))
	}

	return new Response(JSON.stringify({ authorization: '' }))
}
