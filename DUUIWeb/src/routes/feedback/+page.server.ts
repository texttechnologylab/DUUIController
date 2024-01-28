import type { Actions } from '@sveltejs/kit'

export const actions: Actions = {
	send: async ({ request }) => {
		const data = await request.formData()
		if (data === null) return

		const requirements: number = +(data.get('requirements') || 1) - 1
		const frustrating: number = 7 - +(data.get('frustrating') || 1)
		const ease: number = +(data.get('ease') || 1) - 1
		const correction: number = 7 - +(data.get('correction') || 1)
	
	}
}
