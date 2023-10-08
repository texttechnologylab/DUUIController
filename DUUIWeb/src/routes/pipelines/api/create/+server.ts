export async function POST({ request, cookies }) {
	const data = await request.json()
	const response = await fetch('http://192.168.2.122:2605/pipelines', {
		method: 'POST',
		mode: 'cors',
		body: JSON.stringify(data),
		headers: {
			session: cookies.get('session')
		}
	})

	return response
}
