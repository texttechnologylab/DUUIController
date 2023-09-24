import type { DUUIPipeline } from '$lib/data'
import { redirect } from '@sveltejs/kit'
import type { PageServerLoad } from './$types'

export const load: PageServerLoad = async ({ fetch, locals }) => {
	if (!locals.user) {
		throw redirect(300, '/user/login')
	}

	const loadPipelines = async (): Promise<{ pipelines: DUUIPipeline[] }> => {
		const result = await fetch('http://127.0.0.1:2605/pipelines/all/' + locals.user.id, {
			method: 'GET',
			mode: 'cors'
		})
		return await result.json()
	}

	return {
		pipelines: (await loadPipelines()).pipelines
	}
}
