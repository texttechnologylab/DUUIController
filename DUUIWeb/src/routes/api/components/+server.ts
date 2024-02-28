import { API_URL } from '$env/static/private'

/**
 * Attempts to insert a component.
 * @returns the inserted component or the error.
 */
export const POST = async ({ request, cookies }) => {
	const component = await request.json()

	const response = await fetch(`${API_URL}/components`, {
		method: 'POST',
		
		body: JSON.stringify(component),
		headers: {
			Authorization: cookies.get('session') || ''
		}
	})

	return response
}

/**
 * Attempts to fetch components.
 * @returns the component or null.
 */
export const GET = async ({ cookies }) => {
	const response = await fetch(`${API_URL}/components`, {
		method: 'GET',
		
		headers: {
			Authorization: cookies.get('session') || ''
		}
	})

	return response
}

/**
 * Attempts to update a component by its id.
 * @returns the updated component.
 */
export const PUT = async ({ request, cookies }) => {
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
export const DELETE = async ({ request, cookies }) => {
	const data = await request.json()

	const response = await fetch(`${API_URL}/components/${data.oid}`, {
		method: 'DELETE',
		
		headers: {
			Authorization: cookies.get('session') || ''
		}
	})

	return response
}
