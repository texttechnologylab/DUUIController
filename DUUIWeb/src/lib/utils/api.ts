export const Api = {
	Pipelines: '/pipelines/api',
	Services: '/pipelines/api/service'
}

export const makeApiCall = async (endpoint: string, method: string, body: Object) => {
	return await fetch(endpoint, {
		method: method,
		body: JSON.stringify(body)
	})
}
