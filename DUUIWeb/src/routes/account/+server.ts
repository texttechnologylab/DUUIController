import { json } from '@sveltejs/kit'

export async function PUT({ cookies }) {
	cookies.set('just_registered', 'false', {
		path: '/'
	})
	return json({})
}
