import { API_URL } from '$env/static/private'
import { type RequestHandler } from '@sveltejs/kit'

export const GET: RequestHandler = async ({ url, cookies }) => {
	const id: string = url.searchParams.get('id') || ''

	const response = await fetch(`${API_URL}/pipelines/${id}?statistics=true`, {
		method: 'GET',
		headers: {
			Authorization: cookies.get('session') || ''
		}
	})

	return response
}

export async function DELETE({ request, cookies }) {
	const data = await request.json()
	const component: boolean = data.component || false

	const response = await fetch(`${API_URL}/${component ? 'components' : 'pipelines'}/${data.oid}`, {
		method: 'DELETE',
		mode: 'cors',
		headers: {
			Authorization: cookies.get('session') || ''
		}
	})

	return response
}

export async function PUT({ request, cookies }) {
	const data = await request.json()

	const response = await fetch(`${API_URL}/pipelines/${data.oid}`, {
		method: 'PUT',
		mode: 'cors',
		body: JSON.stringify(data),
		headers: {
			Authorization: cookies.get('session') || ''
		}
	})

	return response
}

export async function POST({ request, cookies, url }) {
	const pipeline = await request.json()
	const isTemplate: boolean = (url.searchParams.get('template') || 'false') === 'true'

	const response = await fetch(`${API_URL}/pipelines?template=${isTemplate}`, {
		method: 'POST',
		mode: 'cors',
		body: JSON.stringify(pipeline),
		headers: {
			Authorization: cookies.get('session') || ''
		}
	})

	return response
}
