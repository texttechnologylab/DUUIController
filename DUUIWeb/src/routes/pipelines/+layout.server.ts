import { redirect } from '@sveltejs/kit'
import type { LayoutServerLoad } from '../$types'
import { handleLoginRedirect } from '$lib/utils'

export const load: LayoutServerLoad = async ({ locals, url, cookies }) => {
	
	if (!locals.user) {
		throw redirect(302, handleLoginRedirect(url))
	}
}
