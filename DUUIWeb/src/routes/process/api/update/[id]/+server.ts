import { API_URL } from '$lib/config'
import { json, type RequestHandler } from '@sveltejs/kit'

export const GET: RequestHandler = async (event) => {
	const { cookies } = event
	const { id } = event.params
	const searchParams = event.url.searchParams

	const keys: string[] = ['name', 'progress', 'status', 'size', 'duration']

	let limit: number = Math.min(+(searchParams.get('limit') || '10'), 50)
	let skip: number = Math.max(0, +(searchParams.get('skip') || '0'))
	let sort: string = searchParams.get('sort') || 'name'
	if (!keys.includes(sort)) {
		sort = 'name'
	}

	let order: number = searchParams.get('order') === '1' ? 1 : -1

	let text: string = searchParams.get('text') || ''
	let statusFilters: string = searchParams.get('status') || 'Any'

	const fetchProcess = async () => {
		const response = await fetch(`${API_URL}/processes/${id}`, {
			method: 'GET',
			mode: 'cors',
			headers: {
				Authorization: cookies.get('session') || ''
			}
		})

		return await response.json()
	}

	const fetchTimeline = async () => {
		const response = await fetch(`${API_URL}/processes/${id}/events`, {
			method: 'GET',
			mode: 'cors',
			headers: {
				Authorization: cookies.get('session') || ''
			}
		})

		const json = await response.json()
		return json.timeline
	}

	const fetchDocuments = async () => {
		const response = await fetch(
			`${API_URL}/processes/${id}/documents
			?limit=${limit}
			&skip=${skip}
			&sort=${sort}
			&order=${order}
			&text=${text}
			&status=${statusFilters}`,
			{
				method: 'GET',
				mode: 'cors',
				headers: {
					Authorization: cookies.get('session') || ''
				}
			}
		)

		const json = await response.json()
		return json
	}

	return json({
		process: await fetchProcess(),
		timeline: await fetchTimeline(),
		documents: await fetchDocuments()
	})
}
