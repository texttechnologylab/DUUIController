import { API_URL } from '$lib/config'
import type { DUUIPipeline } from '$lib/duui/pipeline'
import type { DUUIProcess } from '$lib/duui/process'
import type { PageServerLoad } from './$types'

export const load: PageServerLoad = async ({ params, cookies, url, locals}) => {
	
	const loadPipeline = async (): Promise<DUUIPipeline> => {
		const result = await fetch(API_URL + '/pipelines/' + params.oid, {
			method: 'GET',
			mode: 'cors',
			headers: {
				authorization: cookies.get('session') || ''
			}
		})
		return await result.json()
	}

	const loadProcesses = async (): Promise<DUUIProcess[]> => {
		const result = await fetch(
			API_URL +
				'/pipelines/' +
				params.oid +
				`/processes
				?limit=${url.searchParams.get('limit') || 0}
				&offset=${url.searchParams.get('offset') || 0}`,
			{
				method: 'GET',
				mode: 'cors',
				headers: {
					authorization: cookies.get('session') || ''
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
