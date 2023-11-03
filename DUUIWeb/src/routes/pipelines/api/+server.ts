import { API_URL } from '$lib/config'

export async function DELETE({ request, cookies }) {
	const data = await request.json()
	const component: boolean = data.component || false

	const response = await fetch(`${API_URL}/${component ? 'components' : 'pipelines'}/${data.oid}`, {
		method: 'DELETE',
		mode: 'cors',
		headers: {
			session: cookies.get('session') || ''
		}
	})

	return response
}

export async function PUT({ request, cookies }) {
	const data = await request.json()

	const response = await fetch(API_URL + '/pipelines/' + data.oid, {
		method: 'PUT',
		mode: 'cors',
		body: JSON.stringify(data),
		headers: {
			session: cookies.get('session') || ''
		}
	})

	return response
}

export async function POST({ request, cookies }) {
	const data = await request.json()

	const response = await fetch(API_URL + '/pipelines', {
		method: 'POST',
		mode: 'cors',
		body: JSON.stringify(data),
		headers: {
			session: cookies.get('session') || ''
		}
	})

	return response
}
