import { SERVER_API_KEY } from '$env/static/private'
import { API_URL } from '$lib/config'
import type { Handle, RequestEvent } from '@sveltejs/kit'

const fetchUser = async (
	session: string,
	event: RequestEvent<Partial<Record<string, string>>, string | null>
) => {
	const response = await fetch(`${API_URL}/users/auth/`, {
		method: 'GET',
		mode: 'cors',
		headers: {
			Authorization: session,
			key: SERVER_API_KEY
		}
	})

	if (response.ok) {
		const json = await response.json()

		event.locals.user = json.user
	}
}

export const handle: Handle = async ({ event, resolve }) => {
	const { cookies } = event
	let session = cookies.get('session') || event.url.searchParams.get('state') || ''

	if (session) {
		try {
			await fetchUser(session, event)
		} catch (err) {}
	}

	if (!event.locals.user) cookies.delete('session', { path: '/' })

	return await resolve(event)
}
