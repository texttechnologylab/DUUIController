export async function PUT({ cookies }) {
	cookies.delete('session', { path: '/' })
	return new Response(JSON.stringify({ message: 'Logged out' }))
}
