export async function PUT({ cookies }) {
	cookies.set('session', '', {
		path: '/',
		httpOnly: true,
		sameSite: 'strict',
		secure: process.env.NODE_ENV === 'production',
		expires: new Date(0)
	})
	return new Response(JSON.stringify({ message: 'Logged out' }))
}
