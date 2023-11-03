import { equals } from './text'

export const Api = {
	Pipelines: '/pipelines/api',
	Services: '/pipelines/api/service',
	Templates: '/pipelines/api/components',
	Components: '/pipelines/api/components',
	Processes: '/process/api'
}

export const makeApiCall = async (endpoint: string, method: string, body: Object) => {
	if (equals(method, 'GET')) {
		return await fetch(endpoint, {
			method: method
		})
	} else {
		return await fetch(endpoint, {
			method: method,
			body: JSON.stringify(body)
		})
	}
}
