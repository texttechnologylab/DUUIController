import { API_URL } from '$lib/config'
import type { DUUIPipeline } from '$lib/duui/pipeline'
import type { DUUIProcess } from '$lib/duui/process'
import type { PageServerLoad } from './$types'

export const load: PageServerLoad = async ({ params, cookies, url }) => {
	const loadPipeline = async (): Promise<DUUIPipeline> => {
		const response = await fetch(`${API_URL}/pipelines/${params.oid}`, {
			method: 'GET',
			mode: 'cors',
			headers: {
				Authorization: cookies.get('session') || ''
			}
		})

		return await response.json()
	}

	const loadProcesses = async (): Promise<{ processes: DUUIProcess[]; count: number }> => {
		const result = await fetch(
			`${API_URL}/processes?pipeline_id=${params.oid}
				&limit=${url.searchParams.get('limit') || 20}
				&offset=${url.searchParams.get('offset') || 0}
				&sort=started_at
				&order=-1`,
			{
				method: 'GET',
				mode: 'cors',
				headers: {
					Authorization: cookies.get('session') || ''
				}
			}
		)

		const json = await result.json()
		return {
			processes: json.processes,
			count: json.count
		}
	}

	return {
		pipeline: await loadPipeline(),
		processInfo: await loadProcesses()
	}
}
