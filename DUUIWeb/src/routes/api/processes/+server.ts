import { API_URL } from '$env/static/private'
import { json } from '@sveltejs/kit'

export async function GET({ cookies, url, fetch }) {
	const processId = url.searchParams.get('process_id') || ''

	const response = await fetch(`${API_URL}/processes/${processId}`, {
		method: 'GET',
		headers: {
			Authorization: cookies.get('session') || ''
		}
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

export async function POST({ request, cookies, fetch }) {
	const data = await request.json()

	const response = await fetch(`${API_URL}/processes`, {
		method: 'POST',
		body: JSON.stringify(data),
		headers: {
			Authorization: cookies.get('session') || ''
		}
	})

	return response
}

export async function PUT({ request, cookies, fetch }) {
	const data = await request.json()

	const response = await fetch(`${API_URL}/processes/${data.oid}`, {
		method: 'PUT',

		body: JSON.stringify(data),
		headers: {
			Authorization: cookies.get('session') || ''
		}
	})

	return response
}

export async function DELETE({ request, cookies, fetch }) {
	const data = await request.json()

	const response = await fetch(`${API_URL}/processes/${data.oid}`, {
		method: 'DELETE',

		headers: {
			Authorization: cookies.get('session') || ''
		}
	})

	return response
}
