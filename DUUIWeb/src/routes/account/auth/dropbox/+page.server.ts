import { DropboxAuth } from 'dropbox'
import type { PageServerLoad } from './$types'

export const load: PageServerLoad = async ({ locals, cookies }) => {
	

	return {
		user: locals.user,
		session: cookies.get('session')
	}
}
