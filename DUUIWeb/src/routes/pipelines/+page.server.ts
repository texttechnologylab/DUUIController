import type { DUUIPipeline } from '$lib/data'
import { redirect } from '@sveltejs/kit'
import type { PageServerLoad } from './$types'

export const load: PageServerLoad = async ({ fetch, locals, cookies }) => {
	if (!locals.user) {
		throw redirect(300, '/user/login')
	}

	console.log(locals.user)

	const loadPipelines = async (): Promise<{ pipelines: DUUIPipeline[] }> => {
		const result = await fetch('http://192.168.2.122:2605/pipelines/all/' + locals.user.id, {
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
