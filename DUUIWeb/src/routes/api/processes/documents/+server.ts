import { API_URL } from '$env/static/private'
import type { DUUIDocument } from '$lib/duui/io'
import { error, json, type RequestHandler } from '@sveltejs/kit'

/**
 * Sends a get request to the backend to retrieve one or multiple documents.
 */
export const GET: RequestHandler = async ({ cookies, fetch, url }) => {
	const searchParams = url.searchParams

	const processId: string = searchParams.get('process_id') || ''

	if (!processId) error(400, 'Missing process_id query parameter.')

	const keys: string[] = ['name', 'progress', 'status', 'size', 'duration']

	let limit: number = Math.min(+(searchParams.get('limit') || '10'), 50)
	let skip: number = Math.max(0, +(searchParams.get('skip') || '0'))
	let sort: string = searchParams.get('sort') || 'name'
	if (!keys.includes(sort)) {
		sort = 'name'
	}

	let order: number = searchParams.get('order') === '1' ? 1 : -1

	let text: string = searchParams.get('search') || ''
	let statusFilters: string = searchParams.get('status') || 'Any'

	const fetchDocuments = async (): Promise<{
		documents: DUUIDocument[]
		pipelineProgress: Object
		count: number
	}> => {
		const response = await fetch(
			`${API_URL}/processes/${processId}/documents
			?limit=${limit}
			&skip=${skip}
			&sort=${sort}
			&order=${order}
			&search=${text}
			&status=${statusFilters}`,
			{
				method: 'GET',
				headers: {
					Authorization: cookies.get('session') || ''
				}
			}
		)

		if (response.ok) {
			const json = await response.json()
			return json
		}

		return {
			documents: [],
			count: 0,
			pipelineProgress: {}
		}
	}

	return json({
		...(await fetchDocuments())
	})
}
