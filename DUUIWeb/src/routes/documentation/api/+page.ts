import type { PageLoad } from './$types'


// This is the full list of endpoints visualized on the page.
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
			parameters: [
				{ name: 'id', description: "The pipeline's id.", type: 'Query' },
				{
					name: 'statistics',
					description: 'Wether to include statistic for the pipeline. Default is false.',
					type: 'Query'
				},
				{
					name: 'components',
					description: 'Wether to include components. Default is true.',
					type: 'Query'
				}
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
			parameters: [
				{ name: 'limit', description: 'The maximum number of pipelines to return.', type: 'Query' },
				{
					name: 'skip',
					description: 'The pipelines to skip before a limit is applied.',
					type: 'Query'
				},
				{
					name: 'sort',
					description:
						'The field to sort by. Can be name, description, created_at, modified_at, status and times_used.',
					type: 'Query'
				},
				{
					name: 'order',
					description: 'The order to sort by. 1 is ascending and -1 is descending.',
					type: 'Query'
				},
				{
					name: 'statistics',
					description: 'Wether to include statistic for the pipeline. Default is false.',
					type: 'Query'
				},
				{
					name: 'components',
					description: 'Wether to include components. Default is true.',
					type: 'Query'
				}
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
			description:
				'Create a new Pipeline. The body must at least contain the fields listed in parameters.',
			returns: [
				{ code: 200, message: 'Inserted' },
				{ code: 400, message: 'Missing field' }
			],
			parameters: [
				{ name: 'name', description: 'The name of the pipeline.', type: 'Body' },
				{ name: 'description', description: 'The description of the pipeline.', type: 'Body' },
				{ name: 'tags', description: 'An array of tags to categorize the pipeline.', type: 'Body' },
				{
					name: 'components',
					description:
						"An array of components that must follow the structure as described in the section 'components'",
					type: 'Body'
				}
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
			route: '/pipelines/id/start',
			description: 'Instantiate a pipeline and wait idle for process requests.',
			returns: [
				{ code: 200, message: 'Instantiated' },
				{ code: 500, message: 'Not instantiated' }
			],
			parameters: [{ name: 'id', description: "The pipeline's id.", type: 'Query' }],
			exampleRequest: `const response = await fetch('/pipelines/65b3db5c8c997c4ce3c4efb3/start', {
	method: 'POST',
	headers: {
		Authorization: API KEY HERE
	}
})`
		},
		{
			method: 'PUT' as APIMethod,
			route: '/pipelines/id/stop',
			description: 'Shut down an instantiated pipeline. This also cancels running processes.',
			returns: [
				{ code: 200, message: 'Shut down' },
				{ code: 404, message: 'Not found' },
				{ code: 500, message: 'Not shut down' }
			],
			parameters: [{ name: 'id', description: "The pipeline's id.", type: 'Query' }],
			exampleRequest: `const response = await fetch('/pipelines/65b3db5c8c997c4ce3c4efb3/stop', {
	method: 'PUT',
	headers: {
		Authorization: API KEY HERE
	}
})`
		},
		{
			method: 'PUT' as APIMethod,
			route: '/pipelines/id',
			description:
				'Update a pipeline given its id. The body should be a JSON string defining updates.',
			returns: [
				{ code: 201, message: 'Updated' },
				{ code: 400, message: 'Invalid field' },
				{ code: 404, message: 'Not found' }
			],
			parameters: [{ name: 'id', description: "The pipeline's id.", type: 'Query' }],
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
			route: '/pipelines/id',
			description: 'Delete a pipeline given its id.',
			returns: [
				{ code: 200, message: 'Deleted' },
				{ code: 404, message: 'Not found' },
				{ code: 500, message: 'Not deleted' }
			],
			parameters: [{ name: 'id', description: "The pipeline's id.", type: 'Query' }],
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
			parameters: [{ name: 'id', description: "The pipeline's id.", type: 'Query' }],
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
			description: 'Retrieve multiple components.',
			returns: [
				{ code: 200, message: 'Components' },
				{ code: 400, message: 'Missing parameter' },
				{ code: 404, message: 'Not found' }
			],
			parameters: [
				{
					name: 'limit',
					description: 'The maximum number of components to return.',
					type: 'Query'
				},
				{
					name: 'skip',
					description: 'The pipelines to skip before a limit is applied.',
					type: 'Query'
				},
				{
					name: 'sort',
					description:
						'The field to sort by. Can be name, description, created_at, modified_at, status, driver and target.',
					type: 'Query'
				},
				{
					name: 'order',
					description: 'The order to sort by. 1 is ascending and -1 is descending.',
					type: 'Query'
				}
			],
			exampleRequest: `const response = await fetch('/components?limit=5&sort=name&order=1', {
	method: 'GET',
	headers: {
		Authorization: API KEY HERE
	}
})`
		},
		{
			method: 'POST' as APIMethod,
			route: '/components',
			description: 'Create a new component from the fields in the body.',
			returns: [
				{ code: 200, message: 'Inserted' },
				{ code: 400, message: 'Missing field' }
			],
			parameters: [
				{
					name: 'pipeline_id',
					description: 'The id of the pipeline to add the component to.',
					type: 'Body'
				},
				{ name: 'name', description: 'The name of the component.', type: 'Body' },
				{ name: 'description', description: 'The description of the component.', type: 'Body' },
				{
					name: 'tags',
					description: 'An array of tags to categorize the component.',
					type: 'Body'
				},
				{
					name: 'driver',
					description: 'The driver of the component',
					type: 'Body'
				},
				{
					name: 'target',
					description: 'The target (class path, docker image name or url) of the component',
					type: 'Body'
				},
				{
					name: 'options',
					description:
						'An object containing settings for the component. These settings are optional and the default values can be seen in the example request.',
					type: 'Body'
				},
				{
					name: 'parameters',
					description: 'An object containing extra parameters for the component.',
					type: 'Body'
				}
			],
			exampleRequest: `const response = await fetch('/components', {
	method: 'POST',
	body: JSON.stringify({
		pipeline_id: '65b3db5c8c997c4ce3c4efb3',
		name: 'Component Name',
		description: 'A simple component for tokenization.',
		driver: 'DUUIUIMADriver',
		target: 'de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter',
		options: {
			scale: 1,
			use_GPU: true,
			docker_image_fetching: true,
			host: null,
			registry_auth: {
				username: null,
				password: null
			},
			keep_alive: false,
			ignore_200_error: true,
			constraint: [],
			labels: []
		}
	}),
	headers: {
		Authorization: API KEY HERE
	}
})`
		},
		{
			method: 'PUT' as APIMethod,
			route: '/components/id',
			description:
				'Update a component given its id. The body should be a JSON string defining updates.',
			returns: [
				{ code: 201, message: 'Updated' },
				{ code: 400, message: 'Invalid field' },
				{ code: 404, message: 'Not found' }
			],
			parameters: [{ name: 'id', description: "The component's id.", type: 'Query' }],
			exampleRequest: `const response = await fetch('/components/65b3db5c8c997c4ce3c4efb3', {
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
			route: '/components/id',
			description: 'Delete a component given its id.',
			returns: [
				{ code: 200, message: 'Deleted' },
				{ code: 404, message: 'Not found' },
				{ code: 500, message: 'Not deleted' }
			],
			parameters: [{ name: 'id', description: "The component's id.", type: 'Query' }],
			exampleRequest: `const response = await fetch('/components/65b3db5c8c997c4ce3c4efb3', {
	method: 'DELETE',
	headers: {
		Authorization: API KEY HERE
	}
})`
		}
	],
	processes: [
		{
			method: 'GET' as APIMethod,
			route: '/processes/id',
			description: 'Retrieve a process by its id.',
			returns: [
				{ code: 200, message: 'Process' },
				{ code: 404, message: 'Not found' }
			],
			parameters: [{ name: 'id', description: "The process' id.", type: 'Query' }],
			exampleRequest: `const response = await fetch('/processes/65b3dba48c997c4ce3c4f09e', {
	method: 'GET',
	headers: {
		Authorization: API KEY HERE
	}
})`
		},
		{
			method: 'GET' as APIMethod,
			route: '/processes',
			description:
				'Retrieve multiple processes. Requires pipeline_id as a query parameters. Accepts limit, skip, sort, order, status, input and output query parameters.',
			returns: [
				{ code: 200, message: 'Processes' },
				{ code: 400, message: 'Missing parameter pipeline_id' }
			],
			parameters: [
				{ name: 'limit', description: 'The maximum number of processes to return.', type: 'Query' },
				{
					name: 'skip',
					description: 'The processes to skip before a limit is applied.',
					type: 'Query'
				},
				{
					name: 'sort',
					description:
						'The field to sort by. Can be input.provider, output.provider, started_at, duration, count, progress and status.',
					type: 'Query'
				},
				{
					name: 'order',
					description: 'The order to sort by. 1 is ascending and -1 is descending.',
					type: 'Query'
				},
				{
					name: 'status',
					description: "A set of status names separated by ';' to filter by.",
					type: 'Query'
				},
				{
					name: 'input',
					description:
						"A set of input providers separated by ';' to filter by. Accepts (Dropbox, Minio, Text, File, None).",
					type: 'Query'
				},
				{
					name: 'output',
					description:
						"A set of output providers separated by ';' to filter by. Accepts (Dropbox, Minio, None).",
					type: 'Query'
				}
			],
			exampleRequest: `const response = await fetch('/processes?pipeline_id=65b3db5c8c997c4ce3c4efb3&limit=10&sort=input.provider&order=-1status=Completed;Failed', {
	method: 'GET',
	headers: {
		Authorization: API KEY HERE
	}
})`
		},
		{
			method: 'GET' as APIMethod,
			route: '/processes/id/documents',
			description:
				'Retrieve one or multiple documents belonging to a process. Accepts limit, skip, sort, order, status, and search parameters.',
			returns: [
				{ code: 200, message: 'Documents' },
				{ code: 404, message: 'Not found' }
			],
			parameters: [
				{ name: 'limit', description: 'The maximum number of documents to return.', type: 'Query' },
				{
					name: 'skip',
					description: 'The documents to skip before a limit is applied.',
					type: 'Query'
				},
				{
					name: 'sort',
					description: 'The field to sort by. Can be name, progress, status, size and duration.',
					type: 'Query'
				},
				{
					name: 'order',
					description: 'The order to sort by. 1 is ascending and -1 is descending.',
					type: 'Query'
				},
				{
					name: 'status',
					description: "A set of status names separated by ';' to filter by.",
					type: 'Query'
				},
				{
					name: 'search',
					description:
						'A string of text to filter by. The text is compared to the path of the document.',
					type: 'Query'
				}
			],
			exampleRequest: `const response = await fetch('/processes?status=Completed;Failed&search=example.txt&sort=size&limit=3', {
	method: 'GET',
	headers: {
		Authorization: API KEY HERE
	}
})`
		},
		{
			method: 'GET' as APIMethod,
			route: '/processes/id/events',
			description: 'Retrieve events for a processes given its id.',
			returns: [
				{ code: 200, message: 'Events' },
				{ code: 404, message: 'Not found' }
			],
			parameters: [{ name: 'id', description: "The processes' id.", type: 'Query' }],
			exampleRequest: `const response = await fetch('/pipelines/65b3dba48c997c4ce3c4f09e', {
	method: 'GET',
	headers: {
		Authorization: API KEY HERE
	}
})`
		},
		{
			method: 'POST' as APIMethod,
			route: '/processes',
			description: 'Create a new process with the settings provided in the body.',
			returns: [
				{ code: 200, message: 'Started' },
				{ code: 400, message: 'IOException' },
				{ code: 404, message: 'Pipeline not found' },
				{ code: 429, message: 'Not enough resources' },
				{ code: 500, message: 'Failed' }
			],
			parameters: [
				{
					name: 'pipeline_id',
					description: 'The id of the pipeline to execute with this process. This is required.',
					type: 'Body'
				},
				{
					name: 'input',
					description:
						'Sets the source location of documents to process. Dropbox and Minio require a path and file_extension to be specified.',
					type: 'Body'
				},
				{
					name: 'output',
					description:
						'Sets the output location of documents. Dropbox and Minio require a path and file_extension to be specified.',
					type: 'Body'
				},
				{
					name: 'settings',
					description: 'Process specific settings that influence its behavior.',
					type: 'Body'
				}
			],
			exampleRequest: `const response = await fetch('/processes', {
	method: 'POST',
	body: JSON.stringify({
		pipeline_id: '65b3db5c8c997c4ce3c4efb3',
		input: {
			provider: 'Minio',
			path: '/input-bucket',
			file_extension: '.txt'
		}, 
		output: {
			provider: 'Dropbox',
			path: '/output-bucket/path/to/folder',
			file_extension: '.xmi'
		},
		settings: {
			minimum_size: 5000, // Bytes
			recursive: true, // Find files recursively in the input bucket.
			check_target: true, // Check the output location for existing files hat won't be processed. 
			sort_by_size: false, // Sort files in ascending order.
			overwrite: false, // Overwrite existing files on conflict.
			ignore_errors: true, // Skips to the next document instead of failing the entire pipeline in case of error.
			worker_count: 4 // The number of threads to use for processing. Limited by the server.
		}
	})
	headers: {
		Authorization: API KEY HERE
	}
})`
		},
		{
			method: 'PUT' as APIMethod,
			route: '/processes/id',
			description: 'Stop a process and request a cancellation of an active pipeline.',
			returns: [
				{ code: 200, message: 'Shut down' },
				{ code: 404, message: 'Not found' },
				{ code: 500, message: 'Not shut down' }
			],
			parameters: [{ name: 'id', description: "The process' id.", type: 'Query' }],
			exampleRequest: `const response = await fetch('/processes/65b3dba48c997c4ce3c4f09e', {
	method: 'PUT',
	headers: {
		Authorization: API KEY HERE
	}
})`
		},
		{
			method: 'DELETE' as APIMethod,
			route: '/processes/id',
			description: 'Delete a processes given its id.',
			returns: [
				{ code: 200, message: 'Deleted' },
				{ code: 404, message: 'Not found' },
				{ code: 500, message: 'Not deleted' }
			],
			parameters: [{ name: 'id', description: "The processes' id.", type: 'Query' }],
			exampleRequest: `const response = await fetch('/pipelines/65b3dba48c997c4ce3c4f09e', {
	method: 'DELETE',
	headers: {
		Authorization: API KEY HERE
	}
})`
		}
	]
}

export const load: PageLoad = async () => {
	return { endpoints: endpoints }
}
