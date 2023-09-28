export async function POST({ request, cookies }) {
	const data = await request.json()
	const response = await fetch('http://127.0.0.1:2605/pipelines/' + data.id, {
		method: 'PUT',
		mode: 'cors',
		body: JSON.stringify(data),
		headers: {
			session: cookies.get('session') || ''
		}
	})

	return response
}
