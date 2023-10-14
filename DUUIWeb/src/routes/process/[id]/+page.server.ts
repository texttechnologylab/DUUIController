import { API_URL } from '$lib/config'
import type { DUUIPipeline, DUUIProcess } from '$lib/data'
import { DropboxAuth } from 'dropbox'
import type { PageServerLoad } from './$types'



export const load: PageServerLoad = async ({ params, cookies }) => {
	const response = await fetch(API_URL + '/processes/' + params.id, {
		method: 'GET',
		mode: 'cors'
	})
	const process: DUUIProcess = await response.json()

	const loadPipeline = async (process: DUUIProcess): Promise<DUUIPipeline> => {
		const response = await fetch(API_URL + '/pipelines/' + process.pipeline_id, {
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
