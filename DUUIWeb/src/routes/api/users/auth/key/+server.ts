import { SERVER_API_KEY } from '$env/static/private'
import { API_URL } from '$lib/config'
import { fail } from '@sveltejs/kit'
import { randomBytes } from 'crypto'

const generatKey = (length: number = 64, format: BufferEncoding | undefined = 'base64') => {
	const buffer = randomBytes(length)
	return buffer.toString(format)
}

export const PUT = async ({ locals }) => {
	const user = locals.user

	if (!user) {
		return fail(401, { message: 'Unauthorized' })
	}

	const response = await fetch(`${API_URL}/users/${user.oid}`, {
		method: 'PUT',
		mode: 'cors',
		body: JSON.stringify({ "connections.key": generatKey() }),
		headers: {
			Authorization: SERVER_API_KEY
		}
	})

	return response
}

export const DELETE = async ({ locals }) => {
	const user = locals.user

	if (!user) {
		return fail(401, { message: 'Unauthorized' })
	}

	const response = await fetch(`${API_URL}/users/${user.oid}`, {
		method: 'PUT',
		mode: 'cors',
		body: JSON.stringify({ "connections.key": null }),
		headers: {
			Authorization: SERVER_API_KEY
		}
	})

	return response
}
