import { handleLoginRedirect } from '$lib/utils'
import { redirect } from '@sveltejs/kit'
import type { PageServerLoad } from './auth/login/$types'

export const load: PageServerLoad = async ({ locals, url, cookies }) => {
	
	// http://localhost:5173/account/user/connections
	
	

	return {
		user: locals.user
	}
}
