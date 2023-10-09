import { BASE_URL, type DUUIPipeline, type DUUIProcess } from '$lib/data'
import type { Actions, PageServerLoad } from './$types'

export const load: PageServerLoad = async ({ params, cookies }) => {
	const loadPipeline = async (): Promise<DUUIPipeline> => {
		const result = await fetch(BASE_URL + '/pipelines/' + params.id, {
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
			BASE_URL + '/pipelines/' + params.id + '/processes?limit=10',
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

		const response = await fetch(BASE_URL + '/pipelines/' + id, {
			method: 'DELETE',
			mode: 'cors',
			headers: {
				session: cookies.get('session') || ''
			}
		})

		return response.ok
	}
}
