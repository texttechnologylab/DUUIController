import { DUUIDrivers } from '$lib/duui/component'
import type { PageLoad } from './$types'

const endpoints: { [key: string]: APIEndpoint[] } = {
	pipelines: [
		{
			method: 'GET' as APIMethod,
			route: '/pipelines/id',
			description: 'Retrieve a pipeline by its id.',
			returns: [
				{ code: 200, message: 'Pipeline' },
				{ code: 404, message: 'Not found' }
			],
			exampleRequest: `const response = await fetch('/pipelines/65b3db5c8c997c4ce3c4efb3', {
	method: 'GET',
	headers: {
		Authorization: API KEY HERE
	}
})`
		},
		{
			method: 'GET' as APIMethod,
			route: '/pipelines',
			description:
				'Retrieve multiple pipelines. Accepts limit, skip, sort, order and search query parameters.',
			returns: [
				{ code: 200, message: 'Pipelines' },
				{ code: 400, message: 'Missing parameter' },
				{ code: 404, message: 'Not found' }
			],
			exampleRequest: `const response = await fetch('/pipelines?limit=10&sort=times_used&order=-1', {
	method: 'GET',
	headers: {
		Authorization: API KEY HERE
	}
})`
		},
		{
			method: 'POST' as APIMethod,
			route: '/pipelines',
			description: 'Create a new Pipeline, expects a JSON string.',
			returns: [
				{ code: 200, message: 'Pipeline' },
				{ code: 400, message: 'Missing parameter' }
			],
			exampleRequest: `const response = await fetch('/pipelines?template=false', {
	method: 'POST',
	body: JSON.stringify({
		name: 'Pipeline Name',
		description: 'A simple pipeline to count annotations.',
		components: [{
			name: 'Counter',
			driver: 'DUUIUIMADriver',
			target: 'org.texttechnologylab.DockerUnifiedUIMAInterface.tools.CountAnnotations',
			options: {
				scale: 2
			}
		}],
	}),
	headers: {
		Authorization: API KEY HERE
	}
})`
		},
		{
			method: 'POST' as APIMethod,
			route: '/pipelines/:id/start',
			description: 'Instantiate a pipeline and wait idle for process requests.',
			returns: [],
			exampleRequest: ''
		},
		{
			method: 'PUT' as APIMethod,
			route: '/pipelines/:id/stop',
			description: 'Shutdown an instantiated pipeline. This also cancels running processes.',
			returns: [],
			exampleRequest: ''
		},
		{
			method: 'PUT' as APIMethod,
			route: '/pipelines/:id',
			description: 'Update a pipeline given its id.',
			returns: [],
			exampleRequest: `const response = await fetch('/pipelines/65b3db5c8c997c4ce3c4efb3', {
	method: 'PUT',
	body: JSON.strinfigy({
		name: 'New Name'
	})
	headers: {
		Authorization: API KEY HERE
	}
})`
		},
		{
			method: 'DELETE' as APIMethod,
			route: '/pipelines/:id',
			description: 'Delete a pipeline given its id.',
			returns: [
				{ code: 200, message: 'Deleted' },
				{ code: 500, message: 'Failed' }
			],
			exampleRequest: `const response = await fetch('/pipelines/65b3db5c8c997c4ce3c4efb3', {
	method: 'DELETE',
	headers: {
		Authorization: API KEY HERE
	}
})`
		}
	],
	components: [
		{
			method: 'GET' as APIMethod,
			route: '/components/id',
			description: 'Retrieve a component by its id.',
			returns: [
				{ code: 200, message: 'Component' },
				{ code: 404, message: 'Not found' }
			],
			exampleRequest: `const response = await fetch('/components/65b3db5c8c997c4ce3c4efb3', {
	method: 'GET',
	headers: {
		Authorization: API KEY HERE
	}
})`
		},
		{
			method: 'GET' as APIMethod,
			route: '/components',
			description:
				'Retrieve multiple component. Accepts limit, skip, sort, order and search query parameters. Use this endpoint to fetch templates.',
			returns: [
				{ code: 200, message: 'Components' },
				{ code: 400, message: 'Missing parameter' },
				{ code: 404, message: 'Not found' }
			],
			exampleRequest: `const response = await fetch('/components?limit=10&sort=times_used&order=-1', {
	method: 'GET',
	headers: {
		Authorization: API KEY HERE
	}
})`
		},
		{
			method: 'POST' as APIMethod,
			route: '/components',
			description: 'Create a new Component, expects a JSON string.',
			returns: [
				{ code: 200, message: 'Component' },
				{ code: 400, message: 'Missing parameter' }
			],
			exampleRequest: `const response = await fetch('/components?template=false', {
	method: 'POST',
	body: JSON.stringify({
		pipeline_id: '65b3db5c8c997c4ce3c4efb3',
		name: 'Component Name',
		description: 'A simple component for tokenization.',
		driver: 'DUUIUIMADriver',
		target: 'de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter',
		options: {
			scale: 3
		}
	}),
	headers: {
		Authorization: API KEY HERE
	}
})`
		},
		{
			method: 'PUT' as APIMethod,
			route: '/components/:id',
			description: 'Update a component given its id.',
			returns: [],
			exampleRequest: ''
		},
		{
			method: 'DELETE' as APIMethod,
			route: '/components/:id',
			description: 'Delete a component given its id.',
			returns: [
				{ code: 200, message: 'Deleted' },
				{ code: 500, message: 'Failed' }
			],
			exampleRequest: ''
		}
	],
	processes: []
}

export const load: PageLoad = async () => {
	return { endpoints: endpoints }
}
