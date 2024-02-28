import { API_URL } from '$env/static/private'
import type { PageServerLoad } from './$types'

// const dbxAuth = new DropboxAuth({
// 	clientId: DROPBOX_CLIENT_ID,
// 	clientSecret: DROPBOX_CLIENT_SECRET
// })

// const redirectURI = DROPBOX_REDIRECT

// type OAuthResult = {
// 	access_token: string
// 	refresh_token: string
// }

// const connect = async (code: string, user: User) => {
// 	const token: DropboxResponse<object> = await dbxAuth.getAccessTokenFromCode(redirectURI, code)
// 	const result: OAuthResult = token.result as OAuthResult

// 	const access_token: string = result.access_token
// 	const refresh_token: string = result.refresh_token

// 	const response = await fetch(`${API_URL}/users/${user?.oid}`, {
// 		method: 'PUT',
// 		
// 		body: JSON.stringify({
// 			'connections.dropbox.access_token': access_token,
// 			'connections.dropbox.refresh_token': refresh_token
// 		})
// 	})

// 	return response
// }

export const load: PageServerLoad = async ({ locals, url, cookies }) => {
	const code = url.searchParams.get('code')

	const response = await fetch(`${API_URL}/users/auth/dropbox?code=${code}`, {
		method: 'PUT',
		headers: {
			Authorization: cookies.get('session') || ''
		}
	})

	return {
		success: response.ok
	}
	// redirect(300, `/account?success=${response.ok}`)

	// if (code !== null && locals.user) {
	// 	try {
	// 		await connect(code, locals.user)
	// 	} catch (err) {}
	// }
}
