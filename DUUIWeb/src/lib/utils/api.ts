import { equals } from './text'

export const Api = {
	Pipelines: '/pipelines/api',
	Services: '/pipelines/api/service',
	Templates: '/pipelines/api/components',
	Components: '/pipelines/api/components',
	Processes: '/process/api',
	Documents: '/process/api/documents',
	Logout: '/account/api'
}

export const makeApiCall = async (
	endpoint: string,
	method: string,
	body: Object,
	searchParams: string = ''
) => {
	if (equals(method, 'GET')) {
		return await fetch(searchParams ? `${endpoint}?${searchParams}` : endpoint, {
			method: method,
		})
	} else {
		return await fetch(searchParams ? `${endpoint}?${searchParams}` : endpoint, {
			method: method,
			body: JSON.stringify(body)
		})
	}
}
