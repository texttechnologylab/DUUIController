import type { DUUIPipeline, DUUIProcess } from '$lib/data'
import type { PageServerLoad } from './$types'

export const load: PageServerLoad = async ({ params, cookies }) => {
	const response = await fetch('http://127.0.0.1:2605/processes/' + params.id, {
		method: 'GET',
		mode: 'cors'
	})
	const process = await response.json()

	const loadPipeline = async (process: DUUIProcess): Promise<DUUIPipeline> => {
		const response = await fetch('http://127.0.0.1:2605/pipelines/' + process.pipeline_id, {
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

// 		const response = await fetch('http://192.168.2.122:2605/pipelines/' + id, {
// 					method: 'DELETE',
// 					mode: 'cors'
// 				})

//     	return response.ok
// 	}
// }
