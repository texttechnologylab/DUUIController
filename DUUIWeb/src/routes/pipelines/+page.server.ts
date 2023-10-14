import type { DUUIPipeline } from '$lib/data'
import { redirect } from '@sveltejs/kit'
import type { PageServerLoad } from './$types'
import { API_URL } from '$lib/config'

export const load: PageServerLoad = async ({ fetch, locals, cookies }) => {
	if (!locals.user) {
		throw redirect(300, '/user/login')
	}
	const loadPipelines = async (): Promise<{ pipelines: DUUIPipeline[] }> => {
		const result = await fetch(API_URL + '/pipelines/all/' + locals.user.id, {
			method: 'GET',
			mode: 'cors',
			headers: {
				session: cookies.get('session') || ''
			}
		})
		return await result.json()
	}

	return {
		pipelines: (await loadPipelines()).pipelines
	}
}
