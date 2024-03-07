import { API_URL } from '$env/static/private'
import { json } from '@sveltejs/kit'

/**
 * Attempts to insert a component.
 * @returns the inserted component or the error.
 */
export const POST = async ({ request, cookies, url, fetch }) => {
	const component = await request.json()
	const isTemplate: boolean = (url.searchParams.get('template') || 'false') === 'true'
	const response = await fetch(`${API_URL}/components?template=${isTemplate}`, {
		method: 'POST',
		body: JSON.stringify(component),
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

/**
 * Attempts to fetch components.
 * @returns the component or null.
 */
export const GET = async ({ cookies, fetch }) => {
	const response = await fetch(`${API_URL}/components`, {
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

/**
 * Attempts to update a component by its id.
 * @returns the updated component.
 */
export const PUT = async ({ request, cookies, fetch }) => {
	const data = await request.json()

	const response = await fetch(`${API_URL}/components/${data.oid}`, {
		method: 'PUT',
		body: JSON.stringify(data),
		headers: {
			Authorization: cookies.get('session') || ''
		}
	})

	return response
}

/**
 * Attempts to delete a component.
 * @returns a message if the request succeeded or not.
 */
export const DELETE = async ({ request, cookies, fetch }) => {
	const data = await request.json()

	const response = await fetch(`${API_URL}/components/${data.oid}`, {
		method: 'DELETE',
		headers: {
			Authorization: cookies.get('session') || ''
		}
	})

	return response
}
