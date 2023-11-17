import { API_URL } from '$lib/config'

export async function GET({ url, cookies }) {
	const keys: string[] = ['name', 'progress', 'status', 'size', 'duration']

	let oid: string = url.searchParams.get('oid')
	let limit: number = Math.min(+(url.searchParams.get('limit') || '10'), 10)
	let skip: number = Math.max(0, +(url.searchParams.get('skip') || '0'))
	let sort: string = url.searchParams.get('sort') || 'name'
	if (!keys.includes(sort)) {
		sort = 'name'
	}

	let order: number = url.searchParams.get('order') === '1' ? 1 : -1

	let text: string = url.searchParams.get('text') || ''
	let statusFilters: string = url.searchParams.get('status') || 'Any'

	const response = await fetch(
		`${API_URL}/documents?oid=${oid}&limit=${limit}&skip=${skip}&sort=${sort}&order=${order}&text=${text}&status=${statusFilters}`,
		{
			method: 'GET',
			mode: 'cors',
			headers: {
				authorization: cookies.get('session') || ''
			}
		}
	)

	return response
}

// const documentResponse = await fetch(
// 	API_URL +
// 		'/documents/' +
// 		process.oid +
// 		`/documents?status=${statusFilters}&limit=${itemsPerPage}&offset=${itemsPerPage * pageIndex}`,
// 	{
// 		method: 'GET',
// 		mode: 'cors'
// 	}
// )
