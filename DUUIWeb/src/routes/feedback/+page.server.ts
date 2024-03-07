import { API_URL } from '$env/static/private'
import { handleLoginRedirect } from '$lib/utils'
import { redirect } from '@sveltejs/kit'
import type { PageServerLoad } from './$types'

export const load: PageServerLoad = async ({ fetch, locals, cookies, url }) => {
	if (!locals.user) {
		redirect(302, handleLoginRedirect(url))
	}

	const loadFeedback = async (): Promise<{ feedback: FeedbackResult[] }> => {
		const response = await fetch(`${API_URL}/feedback`, {
			method: 'GET',
			headers: {
				Authorization: cookies.get('session') || ''
			}
		})

		return await response.json()
	}

	if (locals.user?.role !== 'Admin') return { feedback: [] }
	return { ...(await loadFeedback()) }
}
