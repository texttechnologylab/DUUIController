import type { DUUIPipeline, DUUIProcess } from '$lib/data'
import type { Actions, PageServerLoad } from './$types'

export const load: PageServerLoad = async ({ params, cookies }) => {
	const loadPipeline = async (): Promise<DUUIPipeline> => {
		const result = await fetch('http://127.0.0.1:2605/pipelines/' + params.id, {
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
			'http://127.0.0.1:2605/pipelines/' + params.id + '/processes?limit=10',
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

		const response = await fetch('http://192.168.2.122:2605/pipelines/' + id, {
			method: 'DELETE',
			mode: 'cors',
			headers: {
				session: cookies.get('session') || ''
			}
		})

		return response.ok
	}
}
