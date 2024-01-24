import { API_URL } from '$lib/config'
import { includes } from '$lib/duui/utils/text'
import { json, type RequestHandler } from '@sveltejs/kit'

export async function DELETE({ request, cookies }) {
	const data = await request.json()
	const component: boolean = data.component || false

	const response = await fetch(`${API_URL}/${component ? 'components' : 'pipelines'}/${data.oid}`, {
		method: 'DELETE',
		mode: 'cors',
		headers: {
			Authorization: cookies.get('session') || ''
		}
	})

	return response
}

export async function PUT({ request, cookies }) {
	const data = await request.json()

	const response = await fetch(`${API_URL}/pipelines/${data.oid}`, {
		method: 'PUT',
		mode: 'cors',
		body: JSON.stringify(data),
		headers: {
			Authorization: cookies.get('session') || ''
		}
	})

	return response
}

export async function POST({ request, cookies }) {
	const data = await request.json()

	const response = await fetch(`${API_URL}/pipelines?template=${data.template}`, {
		method: 'POST',
		mode: 'cors',
		body: JSON.stringify(data.pipeline),
		headers: {
			Authorization: cookies.get('session') || ''
		}
	})

	return response
}

export const GET: RequestHandler = async (event) => {
	const { cookies } = event
	const searchParams = event.url.searchParams

	const id = searchParams.get('pipeline_id') || ''
	let limit: number = Math.min(+(searchParams.get('limit') || '10'), 50)
	let skip: number = Math.max(+(searchParams.get('skip') || '0'), 0)
	let sort: string = searchParams.get('sort') || 'startTime'
	let order: string = searchParams.get('order') || 'ascending'
	let filter: string = searchParams.get('filter') || 'Any'

	const keys: string[] = ['started_at', 'input', 'count', 'progress', 'status', 'duration']

	if (!keys.includes(sort)) {
		sort = 'started_at'
	}

	const fetchProcesses = async () => {
		const response = await fetch(
			`${API_URL}/processes
			?pipeline_id=${id}
			&limit=${limit}
			&skip=${skip}
			&sort=${sort}
			&order=${order}
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
		processInfo: await fetchProcesses()
	})
}
