import { API_URL } from '$env/static/private'
import { json, type RequestHandler } from '@sveltejs/kit'

/**
 * Sends a get request to the backend to retrieve one or multiple processes.
 */
export const GET: RequestHandler = async ({ cookies, fetch, url }) => {
	const searchParams = url.searchParams

	const id = searchParams.get('pipeline_id') || ''
	let limit: number = Math.min(+(searchParams.get('limit') || '10'), 50)
	let skip: number = Math.max(+(searchParams.get('skip') || '0'), 0)
	let sort: string = searchParams.get('sort') || 'started_at'
	let order: string = searchParams.get('order') || 'ascending'
	let status: string = searchParams.get('status') || 'Any'
	let input: string = searchParams.get('input') || 'Any'
	let output: string = searchParams.get('output') || 'Any'

	const fetchProcesses = async () => {
		const response = await fetch(
			`${API_URL}/processes
			?pipeline_id=${id}
			&limit=${limit}
			&skip=${skip}
			&sort=${sort}
			&order=${order}
			&status=${status}
			&input=${input}
			&output=${output}`,
			{
				method: 'GET',

				headers: {
					Authorization: cookies.get('session') || ''
				}
			}
		)

		return await response.json()
	}

	return json({
		processInfo: await fetchProcesses()
	})
}
