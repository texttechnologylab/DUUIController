import { API_URL } from '$env/static/private'
import type { DUUIPipeline } from '$lib/duui/pipeline'
import { handleLoginRedirect } from '$lib/utils'
import { json, redirect, type RequestHandler } from '@sveltejs/kit'

/**
 * Sends a get request to the backend to retrieve one or more pipelines.
 */
export const GET: RequestHandler = async ({ fetch, locals, url, cookies }) => {
	if (!locals.user) {
		redirect(302, handleLoginRedirect(url))
	}
	const loadPipelines = async (): Promise<{ pipelines: DUUIPipeline[]; count: number }> => {
		const limit = +(url.searchParams.get('limit') || '10')
		const skip = +(url.searchParams.get('skip') || '0')
		const sort = url.searchParams.get('sort') || 'created_at'
		const order = +(url.searchParams.get('order') || '1')

		const response = await fetch(
			`${API_URL}/pipelines
			?limit=${limit}
			&skip=${skip}
			&sort=${sort}
			&order=${order}`,
			{
				method: 'GET',
				headers: {
					Authorization: cookies.get('session') || ''
				}
			}
		)

		return await response.json()
	}

	return json({ ...(await loadPipelines()) })
}
