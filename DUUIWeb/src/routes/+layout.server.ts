import type { LayoutServerLoad } from './$types'

export const load: LayoutServerLoad = async ({ locals, cookies }) => {
	return {
		user: locals.user,
		theme: +(cookies.get('theme') || '0')
	}
}
