import { API_URL } from '$lib/config'
import type { DUUIPipeline } from '$lib/duui/pipeline'
import type { DUUIProcess } from '$lib/duui/process'
import type { PageServerLoad } from './$types'

export const load: PageServerLoad = async ({ params, cookies, url }) => {
	const response = await fetch(`${API_URL}/processes/${params.oid}`, {
		method: 'GET',
		mode: 'cors',
		headers: {
			Authorization: cookies.get('session') || ''
		}
	})

	const process: DUUIProcess = await response.json()

	const loadPipeline = async (process: DUUIProcess): Promise<DUUIPipeline> => {
		const response = await fetch(`${API_URL}/pipelines/${process.pipeline_id}`, {
			method: 'GET',
			mode: 'cors',
			headers: {
				Authorization: cookies.get('session') || ''
			}
		})
		return await response.json()
	}

	const loadDocuments = async () => {
		const keys: string[] = ['name', 'progress', 'status', 'size', 'duration']

		let limit: number = Math.min(+(url.searchParams.get('limit') || '10'), 50)
		let skip: number = Math.max(0, +(url.searchParams.get('skip') || '0'))
		let by: string = url.searchParams.get('by') || 'name'
		if (!keys.includes(by)) {
			by = 'name'
		}

		let order: number = url.searchParams.get('order') === '1' ? 1 : -1
		let text: string = url.searchParams.get('text') || ''
		let filter: string = url.searchParams.get('filter') || 'Any'
		const response = await fetch(
			`${API_URL}/processes/${params.oid}/documents
			?limit=${limit}
			&skip=${skip}
			&sort=${by}
			&order=${order}
			&text=${text}
			&filter=${filter}`,
			{
				method: 'GET',
				mode: 'cors',
				headers: {
					Authorization: cookies.get('session') || ''
				}
			}
		)

		const documentQuery = await response.json()
		
		return documentQuery
	}

	const loadTimeline = async () => {
		const response = await fetch(`${API_URL}/processes/${process.oid}/events`, {
			method: 'GET',
			mode: 'cors',
			headers: {
				Authorization: cookies.get('session') || ''
			}
		})
		const data = await response.json()
		const { timeline } = data

		return timeline
	}

	return {
		process: process,
		pipeline: await loadPipeline(process),
		documentQuery: await loadDocuments(),
		timeline: await loadTimeline()
	}
}
