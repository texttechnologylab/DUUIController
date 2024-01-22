import { redirect } from '@sveltejs/kit'
import type { PageServerLoad } from './$types'
import { API_URL } from '$lib/config'
import type { DUUIPipeline } from '$lib/duui/pipeline'

export const load: PageServerLoad = async ({ fetch, locals, cookies }) => {
	if (!locals.user) {
		redirect(300, '/user/login');
	}
	const loadPipelines = async (): Promise<{ pipelines: DUUIPipeline[] }> => {
		const response = await fetch(`${API_URL}/pipelines?limit=25`, {
			method: 'GET',
			mode: 'cors',
			headers: {
				Authorization: cookies.get('session') || ''
			}
		})

		return await response.json()
	}

	return {
		pipelines: (await loadPipelines()).pipelines
	}
}
