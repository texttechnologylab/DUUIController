import { API_URL } from '$env/static/private'
import type { DUUIPipeline } from '$lib/duui/pipeline'
import type { DUUIProcess } from '$lib/duui/process'
import type { PageServerLoad } from './$types'

export const load: PageServerLoad = async ({ params, cookies, fetch }) => {
	const response = await fetch(`${API_URL}/processes/${params.oid}`, {
		method: 'GET',
		headers: {
			Authorization: cookies.get('session') || ''
		}
	})

	const process: DUUIProcess = await response.json()

	const loadPipeline = async (process: DUUIProcess): Promise<DUUIPipeline> => {
		const response = await fetch(`${API_URL}/pipelines/${process.pipeline_id}`, {
			method: 'GET',
			headers: {
				Authorization: cookies.get('session') || ''
			}
		})
		return await response.json()
	}

	return {
		process: process,
		pipeline: await loadPipeline(process)
	}
}
