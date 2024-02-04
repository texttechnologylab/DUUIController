import { blankPipeline, type DUUIPipeline } from '$lib/duui/pipeline'
import type { PageServerLoad } from './$types'

export const load: PageServerLoad = async () => {
	const examplePipeline: DUUIPipeline = blankPipeline()

	examplePipeline.oid = ''
	examplePipeline.name = 'Example'
	examplePipeline.description = 'You can edit these properties here'
	examplePipeline.components = []
	examplePipeline.status = ''
	examplePipeline.tags = []
	examplePipeline.user_id = '1'

	return {
		examplePipeline: examplePipeline
	}
}
