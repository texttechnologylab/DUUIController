import type { DUUIPipelineComponent } from '$lib/data'

export async function GET() {
	const response = await fetch('http://192.168.2.122:2605/components', {
		method: 'GET',
		mode: 'cors'
	})

	return response
}
