import { redirect } from '@sveltejs/kit'
import type { PageServerLoad } from './$types'
import { API_URL } from '$lib/config'
import type { DUUIPipeline } from '$lib/duui/pipeline'

export const load: PageServerLoad = async ({
	fetch,
	locals,
	url,
	cookies
}): Promise<{ pipelines: DUUIPipeline[]; count: number }> => {
	if (!locals.user) {
		redirect(300, '/user/login')
	}
	const loadPipelines = async (): Promise<{ pipelines: DUUIPipeline[]; count: number }> => {
		const limit = +(url.searchParams.get('limit') || '10')

		const response = await fetch(`${API_URL}/pipelines?limit=${limit}`, {
			method: 'GET',
			mode: 'cors',
			headers: {
				Authorization: cookies.get('session') || ''
			}
		})

		return await response.json()
	}

	return { ...(await loadPipelines()) }
}
