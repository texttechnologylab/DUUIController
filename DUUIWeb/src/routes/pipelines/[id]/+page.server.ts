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

export const actions: Actions = {
	default: async ({ params, cookies }) => {
		let id: string = params.id

		const response = await fetch(API_URL + '/pipelines/' + id, {
			method: 'DELETE',
			mode: 'cors',
			headers: {
				session: cookies.get('session') || ''
			}
		})

		return response.ok
	}
}
