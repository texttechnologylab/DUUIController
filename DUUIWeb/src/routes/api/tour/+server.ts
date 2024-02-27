import { json } from '@sveltejs/kit'

export const POST = async ({ cookies, url }) => {
	const step = url.searchParams.get('tour_step') || '1'
	const do_tour = (url.searchParams.get('do_tour') || 'false') === 'true'

	cookies.set('tour_step', +step + 1, {
		path: '/',
		sameSite: 'lax',
		httpOnly: true,
		secure: process.env.NODE_ENV === 'production',
		maxAge: 60 * 60 * 24 * 30
	})

	cookies.set('do_tour', do_tour, {
		path: '/',
		sameSite: 'lax',
		httpOnly: true,
		secure: process.env.NODE_ENV === 'production',
		maxAge: 60 * 60 * 24 * 30
	})

	return json({})
}
