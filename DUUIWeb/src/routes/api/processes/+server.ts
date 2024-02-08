import { API_URL } from '$env/static/private'

export async function GET({ cookies, url }) {
	const processId = url.searchParams.get('process_id') || ''

	const response = await fetch(`${API_URL}/processes/${processId}`, {
		method: 'GET',
		mode: 'cors',
		headers: {
			Authorization: cookies.get('session') || ''
		}
	})

	return response
}

export async function POST({ request, cookies }) {
	const data = await request.json()

	const response = await fetch(`${API_URL}/processes`, {
		method: 'POST',
		mode: 'cors',
		body: JSON.stringify(data),
		headers: {
			Authorization: cookies.get('session') || ''
		}
	})

	return response
}

export async function PUT({ request, cookies }) {
	const data = await request.json()

	const response = await fetch(`${API_URL}/processes/${data.oid}`, {
		method: 'PUT',
		mode: 'cors',
		body: JSON.stringify(data),
		headers: {
			Authorization: cookies.get('session') || ''
		}
	})

	return response
}

export async function DELETE({ request, cookies }) {
	const data = await request.json()

	const response = await fetch(`${API_URL}/processes/${data.oid}`, {
		method: 'DELETE',
		mode: 'cors',
		headers: {
			Authorization: cookies.get('session') || ''
		}
	})

	return response
}
