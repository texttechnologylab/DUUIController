import { API_URL } from '$lib/config'

export async function POST({ request, cookies, locals }) {
	const { endpoint, accessKey, secretKey } = await request.json()
	const user = locals.user

	const response = await fetch(`${API_URL}/users/${user.oid}/minio`, {
		method: 'PUT',
		mode: 'cors',
		body: JSON.stringify({
			minio: {
				endpoint: endpoint,
				access_key: accessKey,
				secret_key: secretKey
			}
		}),
		headers: {
			Authorization: cookies.get('session') || ''
		}
	})

	return response
}

export async function DELETE({ request, cookies, locals }) {
	const user = locals.user

	const response = await fetch(`${API_URL}/users/${user.oid}`, {
		method: 'PUT',
		mode: 'cors',
		body: JSON.stringify({
			minio: {
				endpoint: null,
				access_key: null,
				secret_key: null
			}
		}),
		headers: {
			Authorization: cookies.get('session') || ''
		}
	})

	return response
}
