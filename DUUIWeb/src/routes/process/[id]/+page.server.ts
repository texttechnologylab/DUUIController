import { BASE_URL, type DUUIPipeline, type DUUIProcess } from '$lib/data'
import type { PageServerLoad } from './$types'

export const load: PageServerLoad = async ({ params, cookies }) => {
	const response = await fetch(BASE_URL + '/processes/' + params.id, {
		method: 'GET',
		mode: 'cors'
	})
	const process: DUUIProcess = await response.json()

	const loadPipeline = async (process: DUUIProcess): Promise<DUUIPipeline> => {
		const response = await fetch(BASE_URL + '/pipelines/' + process.pipeline_id, {
			method: 'GET',
			mode: 'cors',
			headers: {
				session: cookies.get('session') || ''
			}
		})
		return await response.json()
	}

	return {
		process: process,
		pipeline: loadPipeline(process)
	}
}

// export const actions: Actions = {
// 	default: async ({ params }) => {
// 		let id: string = params.id

// 		const response = await fetch(BASE_URL + '/pipelines/' + id, {
// 					method: 'DELETE',
// 					mode: 'cors'
// 				})

//     	return response.ok
// 	}
// }
