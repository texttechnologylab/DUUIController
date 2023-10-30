import { API_URL } from '$lib/config'
import type { DUUIPipeline } from '$lib/duui/pipeline'
import type { DUUIProcess } from '$lib/duui/process'
import type { Actions, PageServerLoad } from './$types'

export const load: PageServerLoad = async ({ params, cookies, url }) => {
	const loadPipeline = async (): Promise<DUUIPipeline> => {
		const result = await fetch(API_URL + '/pipelines/' + params.id, {
			method: 'GET',
			mode: 'cors',
			headers: {
				session: cookies.get('session') || ''
			}
		})
		return await result.json()
	}

	const loadProcesses = async (): Promise<DUUIProcess[]> => {
		const result = await fetch(
			API_URL +
				'/pipelines/' +
				params.id +
				`/processes
				?limit=${url.searchParams.get('limit') || 0}
				&offset=${url.searchParams.get('offset') || 0}`,
			{
				method: 'GET',
				mode: 'cors',
				headers: {
					session: cookies.get('session') || ''
				}
			}
		)
		return (await result.json()).processes
	}

	return {
		pipeline: loadPipeline(),
		processes: loadProcesses()
	}
}
