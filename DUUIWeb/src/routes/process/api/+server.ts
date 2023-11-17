import { API_URL } from '$lib/config'


export async function POST({ request, cookies }) {
	const data = await request.json()
	

	const response = await fetch(`${API_URL}/processes`, {
		method: 'POST',
		mode: 'cors',
		body: data,
		headers: {
			authorization: cookies.get('session') || ''
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
			authorization: cookies.get('session') || ''
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
			authorization: cookies.get('session') || ''
		}
	})

	return response
}
