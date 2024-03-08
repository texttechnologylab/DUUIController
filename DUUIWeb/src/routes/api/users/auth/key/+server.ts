import { API_URL } from '$env/static/private'
import { error, json } from '@sveltejs/kit'
import { randomBytes } from 'crypto'

/**
 * Uses the crypto module to generate a random API key in base64 with a specific length.
 * @param length The length of the API key. Default is 128.
 * @param format The format of the API key. Default is base64.
 * @returns An API key.
 */
const generatKey = (length: number = 128, format: BufferEncoding | undefined = 'base64') => {
	const buffer = randomBytes(length)
	return buffer.toString(format)
}

/**
 * Sends a put request to the backend to update a user's API key.
 */
export const PUT = async ({ locals, fetch }) => {
	const user = locals.user

	if (!user) {
		return error(401, { message: 'Unauthorized' })
	}

	const response = await fetch(`${API_URL}/users/${user.oid}`, {
		method: 'PUT',
		body: JSON.stringify({ 'connections.key': generatKey() })
	})

	if (response.ok) {
		return json(await response.json(), {
			headers: {
				'Content-Type': 'application/json'
			},
			status: 200
		})
	}

	return response
}

/**
 * Sends a delete request to the backend to delete a user's API key.
 */
export const DELETE = async ({ locals }) => {
	const user = locals.user

	if (!user) {
		return error(401, { message: 'Unauthorized' })
	}

	const response = await fetch(`${API_URL}/users/${user.oid}`, {
		method: 'PUT',
		body: JSON.stringify({ 'connections.key': null })
	})

	if (response.ok) {
		return json(await response.json(), {
			headers: {
				'Content-Type': 'application/json'
			},
			status: 200
		})
	}

	return response
}
