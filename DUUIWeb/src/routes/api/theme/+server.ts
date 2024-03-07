import { COLORS } from '$lib/config'
import { json } from '@sveltejs/kit'

/**
 * Sets the theme cookie to the requested theme.
 */
export const PUT = ({ url, cookies }) => {
	let color: string = url.searchParams.get('color') || 'blue'
	let theme: string = '' + (Object.keys(COLORS).indexOf(color) || 0)

	cookies.set('theme', theme, {
		path: '/',
		sameSite: 'lax',
		httpOnly: true,
		secure: process.env.NODE_ENV === 'production',
		maxAge: 60 * 60 * 24 * 30
	})

	return json({ theme: theme })
}
