import { DropboxAuth } from 'dropbox'
import type { PageServerLoad } from './$types'

const dbxAuth = new DropboxAuth({
	clientId: 'l2nw2ign2z8h9hg',
	clientSecret: 'wqgejzv1xivdwki'
})

const redirectURI = `http://localhost:5173/account/auth/dropbox`

export const load: PageServerLoad = async ({ locals, cookies, url }) => {
	const getAuthURL = async () => {
		const response = await dbxAuth.getAuthenticationUrl(
			redirectURI,
			cookies.get('session') || '',
			'code',
			'offline',
			undefined,
			undefined,
			false
		)
		return response
	}

	return {
		dropbBoxURL: getAuthURL(),
		user: locals.user
	}
}
