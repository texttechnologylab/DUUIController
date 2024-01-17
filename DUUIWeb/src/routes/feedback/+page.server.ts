import type { Actions } from '@sveltejs/kit'

export const actions: Actions = {
	send: async ({ request, cookies }) => {
		const data = await request.formData()
	}
}
