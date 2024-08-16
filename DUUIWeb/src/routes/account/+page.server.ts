import { API_URL} from '$env/static/private'
import { handleLoginRedirect } from '$lib/utils'
import { fail, redirect } from '@sveltejs/kit'
import { DropboxAuth } from 'dropbox'
import type { PageServerLoad } from './$types'
import { OAuth2Client } from 'google-auth-library'

export const load: PageServerLoad = async ({ locals, cookies, url }) => {
	if (!locals.user) {
		redirect(302, handleLoginRedirect(url))
	}

	// Retrieve the Dropbox OAuth 2.0 credentials from the backend.
	// These correspond to the properties set in the config file.
	const response = await fetch(`${API_URL}/users/auth/dropbox`, {
		method: 'GET'
	})

	let dropbBoxURL = new String('')

	try {
		const credentials: {
			key: string
			secret: string
			url: string
		} = await response.json()

		const dbxAuth = new DropboxAuth({
			clientId: credentials.key,
			clientSecret: credentials.secret
		})

		dropbBoxURL = await dbxAuth.getAuthenticationUrl(
			credentials.url,
			cookies.get('session') || '',
			'code',
			'offline',
			undefined,
			undefined,
			false
		)
	} catch (error) { /* empty */ }


	let googleDriveURL = ""
	const googleResponse = await fetch(`${API_URL}/users/auth/google`, {
		method: 'GET'
	})

	const googleCredentials: {
		key: string
		secret: string
		url: string
	} = await googleResponse.json();

	const googleAuth = new OAuth2Client(
			googleCredentials.key,
			googleCredentials.secret,
			googleCredentials.url)

	googleDriveURL = googleAuth.generateAuthUrl(
		{
						scope: "https://www.googleapis.com/auth/drive openid ",
						access_type: "offline",
						redirect_uri: googleCredentials.url,
						prompt: "select_account"
		})

	/**
	 * Fetch a user from the backend.
	 *
	 * @returns the json object returned from the API call.
	 */
	const fetchProfile = async () => {
		const response = await fetch(`${API_URL}/users/${locals.user?.oid}`, {
			method: 'GET'
		})

		if (!response.ok) {
			return fail(response.status, { message: response.statusText })
		}
		return await response.json()
	}

	/**
	 * Fetch users from the database (only for Admins.)
	 * @returns the json object returned from the API call.
	 */
	const fetchUsers = async () => {
		const response = await fetch(`${API_URL}/users`, {
			method: 'GET',
			headers: {
				Authorization: cookies.get('session') || ''
			}
		})

		if (!response.ok) {
			return fail(response.status, { message: response.statusText })
		}

		return await response.json()
	}

	return {
		dropbBoxURL: dropbBoxURL,
		googleDriveURL: googleDriveURL,
		user: (await fetchProfile()).user,
		theme: +(cookies.get('theme') || '0'),
		users: (await fetchUsers()).users
	}
}