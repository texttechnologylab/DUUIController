import { API_URL } from '$lib/config'
import type { DUUIPipeline } from '$lib/duui/pipeline'
import type { DUUIProcess } from '$lib/duui/process'
import type { PageServerLoad } from './$types'

export const load: PageServerLoad = async ({ params, cookies, url }) => {
	const response = await fetch(`${API_URL}/processes/${params.oid}`, {
		method: 'GET',
		mode: 'cors'
	})

	const process: DUUIProcess = await response.json()

	const loadPipeline = async (process: DUUIProcess): Promise<DUUIPipeline> => {
		const response = await fetch(`${API_URL}/pipelines/${process.pipeline_id}`, {
			method: 'GET',
			mode: 'cors',
			headers: {
				session: cookies.get('session') || ''
			}
		})
		return await response.json()
	}

	const loadDocuments = async () => {
		const keys: string[] = ['name', 'progress', 'status', 'size', 'duration']

		let limit: number = Math.min(+(url.searchParams.get('limit') || '10'), 10)
		let skip: number = Math.max(0, +(url.searchParams.get('skip') || '0'))
		let sort: string = url.searchParams.get('sort') || 'name'
		if (!keys.includes(sort)) {
			sort = 'name'
		}

		let order: number = url.searchParams.get('order') === '1' ? 1 : -1
		let text: string = url.searchParams.get('text') || ''
		let statusFilters: string = url.searchParams.get('status') || 'Any'

		const response = await fetch(
			`${API_URL}/documents?oid=${params.oid}&limit=${limit}&skip=${skip}&sort=${sort}&order=${order}&text=${text}&status=${statusFilters}`,
			{
				method: 'GET',
				mode: 'cors',
				headers: {
					session: cookies.get('session') || ''
				}
			}
		)

		const documentQuery = await response.json()
		return documentQuery
	}

	return {
		process: process,
		pipeline: loadPipeline(process),
		documentQuery: loadDocuments()
	}
}
