import { equals } from './text'

export const Api = {
	Pipelines: '/pipelines/api',
	PipelineTemplates: '/pipelines/api/templates',
	Services: '/pipelines/api/service',
	Templates: '/pipelines/api/components',
	Components: '/pipelines/api/components',
	Processes: '/process/api',
	Documents: '/process/api/documents',
	Logout: '/account/auth/logout',
	Authentication: '/account/api/auth',
	Dropbox: '/auth/dropbox',
	Minio: '/account/api/minio',
	File: '/process/api/upload'
}

export const makeApiCall = async (
	endpoint: string,
	method: string,
	body: Object | undefined | FormData = undefined,
	searchParams: string = '',
	formData: boolean = false
) => {
	if (formData) {
		return await fetch(searchParams ? `${endpoint}?${searchParams}` : endpoint, {
			method: method,
			body: body
		})
	}

	if (equals(method, 'GET')) {
		return await fetch(searchParams ? `${endpoint}?${searchParams}` : endpoint, {
			method: method
		})
	} else {
		return await fetch(searchParams ? `${endpoint}?${searchParams}` : endpoint, {
			method: method,
			body: JSON.stringify(body)
		})
	}
}
