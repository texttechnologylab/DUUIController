import { API_URL } from '$env/static/private'
import { error } from '@sveltejs/kit'
import { randomBytes } from 'crypto'

const generatKey = (length: number = 64, format: BufferEncoding | undefined = 'base64') => {
	const buffer = randomBytes(length)
	return buffer.toString(format)
}

export const PUT = async ({ locals, fetch }) => {
	const user = locals.user

	if (!user) {
		return error(401, { message: 'Unauthorized' })
	}

	const response = await fetch(`${API_URL}/users/${user.oid}`, {
		method: 'PUT',
		
		body: JSON.stringify({ 'connections.key': generatKey() })
	})

	return response
}

export const DELETE = async ({ locals }) => {
	const user = locals.user

	if (!user) {
		return error(401, { message: 'Unauthorized' })
	}

	const response = await fetch(`${API_URL}/users/${user.oid}`, {
		method: 'PUT',
		
		body: JSON.stringify({ 'connections.key': null })
	})

	return response
}
