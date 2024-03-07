import { API_URL } from '$env/static/private'
import type { DUUIComponent } from '$lib/duui/component'
import type { DUUIPipeline } from '$lib/duui/pipeline'
import type { DUUIProcess } from '$lib/duui/process'
import type { PageServerLoad } from './$types'

export const load: PageServerLoad = async ({ params, cookies, url }) => {
	const loadPipeline = async (): Promise<DUUIPipeline> => {
		const response = await fetch(`${API_URL}/pipelines/${params.oid}?statistics=true`, {
			method: 'GET',

			headers: {
				Authorization: cookies.get('session') || ''
			}
		})

		return await response.json()
	}

	const fetchComponentTemplates = async (): Promise<{ components: DUUIComponent[] }> => {
		const response = await fetch(`${API_URL}/components`, {
			method: 'GET',

			headers: {
				Authorization: cookies.get('session') || ''
			}
		})

		if (response.ok) {
			return await response.json()
		}

		return { components: [] }
	}

	const loadProcesses = async (): Promise<{ processes: DUUIProcess[]; count: number }> => {
		let statusFilter: string[] = (url.searchParams.get('status') || 'Any').split(';')
		let inputFilter: string[] = (url.searchParams.get('input') || 'Any').split(';')
		let outputFilter: string[] = (url.searchParams.get('output') || 'Any').split(';')

		const result = await fetch(
			`${API_URL}/processes?pipeline_id=${params.oid}
				&limit=${url.searchParams.get('limit') || 10}
				&offset=${url.searchParams.get('offset') || 0}
				&sort=started_at
				&order=-1
				&status=${statusFilter.join(';')}
				&input=${inputFilter.join(';')}
				&output=${outputFilter.join(';')}`,
			{
				method: 'GET',

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
		processInfo: await loadProcesses(),
		templateComponents: (await fetchComponentTemplates()).components
	}
}
