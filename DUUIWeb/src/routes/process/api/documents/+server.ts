import { API_URL } from '$lib/config'
import { json, type RequestHandler } from '@sveltejs/kit'

export const GET: RequestHandler = async (event) => {
	const { cookies } = event
	const searchParams = event.url.searchParams

	const keys: string[] = ['name', 'progress', 'status', 'size', 'duration']

	let process_id: string = searchParams.get('process_id') || ''
	let limit: number = Math.min(+(searchParams.get('limit') || '10'), 50)
	let skip: number = Math.max(+(searchParams.get('skip') || '0'), 0)
	let sort: string = searchParams.get('by') || 'name'
	if (!keys.includes(sort)) {
		sort = 'name'
	}

	let order: number = searchParams.get('order') === '1' ? 1 : -1

	let text: string = searchParams.get('text') || ''
	let filter: string = searchParams.get('filter') || 'Any'

	const fetchDocuments = async () => {
		
		const response = await fetch(
			`${API_URL}/processes/${process_id}/documents
			?limit=${limit}
			&skip=${skip}
			&sort=${sort}
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
			
		return await response.json()
	}

	return json({
		result: await fetchDocuments()
	})
}
