import { API_URL } from '$env/static/private'
import type { DUUIPipeline } from '$lib/duui/pipeline'
import { handleLoginRedirect } from '$lib/utils'
import { redirect } from '@sveltejs/kit'
import type { PageServerLoad } from './$types'

export const load: PageServerLoad = async ({
	fetch,
	locals,
	url,
	cookies
}): Promise<{ pipelines: DUUIPipeline[]; count: number }> => {
	if (!locals.user) {
		redirect(302, handleLoginRedirect(url))
	}

	const loadPipelines = async (): Promise<{ pipelines: DUUIPipeline[]; count: number }> => {
		const limit = +(url.searchParams.get('limit') || '12')

		const response = await fetch(`${API_URL}/pipelines?limit=${limit}`, {
			method: 'GET',

			headers: {
				Authorization: cookies.get('session') || ''
			}
		})

		return await response.json()
	}

	return { ...(await loadPipelines()) }
}
