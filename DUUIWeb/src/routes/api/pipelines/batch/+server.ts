import { API_URL } from '$env/static/private'
import type { DUUIPipeline } from '$lib/duui/pipeline'
import { type RequestHandler, redirect, json } from '@sveltejs/kit'

export const GET: RequestHandler = async ({ fetch, locals, url, cookies }) => {
	if (!locals.user) {
		redirect(300, '/user/login')
	}
	const loadPipelines = async (): Promise<{ pipelines: DUUIPipeline[]; count: number }> => {
		const limit = +(url.searchParams.get('limit') || '50')

		const response = await fetch(`${API_URL}/pipelines?limit=${limit}`, {
			method: 'GET',
			mode: 'cors',
			headers: {
				Authorization: cookies.get('session') || ''
			}
		})

		return await response.json()
	}

	return json({ ...(await loadPipelines()) })
}
