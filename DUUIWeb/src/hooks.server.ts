import { API_URL } from '$env/static/private'
import type { Handle, RequestEvent } from '@sveltejs/kit'

const fetchUser = async (
	session: string,
	event: RequestEvent<Partial<Record<string, string>>, string | null>
) => {
	const response = await fetch(`${API_URL}/users/auth/`, {
		method: 'GET',
		headers: {
			Authorization: session
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
