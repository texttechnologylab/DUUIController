import { handleLoginRedirect } from '$lib/utils'
import { redirect } from '@sveltejs/kit'
import type { LayoutServerLoad } from '../$types'

export const load: LayoutServerLoad = async ({ locals, url, cookies }) => {
	if (!locals.user) {
		redirect(302, handleLoginRedirect(url))
	}
}
