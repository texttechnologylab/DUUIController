import { API_URL } from '$env/static/private'
import { type RequestHandler } from '@sveltejs/kit'

export const GET: RequestHandler = async ({ url, cookies, fetch }) => {
	const id: string = url.searchParams.get('id') || ''

	const response = await fetch(`${API_URL}/pipelines/${id}?statistics=true`, {
		method: 'GET',
		headers: {
			Authorization: cookies.get('session') || ''
		}
	})

	return response
}

export async function DELETE({ request, cookies, fetch }) {
	const data = await request.json()
	const component: boolean = data.component || false

	const response = await fetch(`${API_URL}/${component ? 'components' : 'pipelines'}/${data.oid}`, {
		method: 'DELETE',

		headers: {
			Authorization: cookies.get('session') || ''
		}
	})

	return response
}

export async function PUT({ request, cookies, fetch }) {
	const data = await request.json()

	const response = await fetch(`${API_URL}/pipelines/${data.oid}`, {
		method: 'PUT',

		body: JSON.stringify(data),
		headers: {
			Authorization: cookies.get('session') || ''
		}
	})

	return response
}

export async function POST({ request, cookies, url, fetch }) {
	const pipeline = await request.json()
	const isTemplate: boolean = (url.searchParams.get('template') || 'false') === 'true'

	const response = await fetch(`${API_URL}/pipelines?template=${isTemplate}`, {
		method: 'POST',

		body: JSON.stringify(pipeline),
		headers: {
			Authorization: cookies.get('session') || ''
		}
	})

	return response
}
