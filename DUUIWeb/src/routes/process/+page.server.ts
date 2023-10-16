import { API_URL } from '$lib/config'
import { DropboxAuth } from 'dropbox'
import type { PageServerLoad } from './$types'

const dbxAuth = new DropboxAuth({
	clientId: 'l2nw2ign2z8h9hg',
	clientSecret: 'wqgejzv1xivdwki'
})

const redirectURI = `http://localhost:5173/user/auth/dropbox`

export const load: PageServerLoad = async ({ locals, cookies, url }) => {
	const checkDropboxAuthorization = async (): Promise<boolean> => {
		if (!locals.user) return new Promise(() => false)

		const response = await fetch(API_URL + '/users/auth/dropbox/' + locals.user.id, {
			method: 'GET',
			mode: 'cors',
			headers: {
				session: cookies.get('session') || ''
			}
		})
		return response.ok
	}

	const getAuthURL = async () => {
		const response = await dbxAuth.getAuthenticationUrl(
			redirectURI,
			url.pathname.split('/').at(-2),
			'code',
			'offline',
			undefined,
			undefined,
			false
		)
		return response
	}

	return {
		dbxURL: getAuthURL(),
		dbxAuthorized: checkDropboxAuthorization(),
		user: locals.user,
		session: cookies.get('session')
	}
}
