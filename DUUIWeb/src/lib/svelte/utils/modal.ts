import { type ModalSettings, type ModalStore } from '@skeletonlabs/skeleton'

/**
 * Show a confirmation modals with the given settings.
 *
 * @param settings The settings for the modal.
 * @param modalStore The Svelte Writable store returned from getModalStore()
 * @returns
 */
export const showConfirmationModal = async (
	settings: ModalSettings['meta'],
	modalStore: ModalStore
) => {
	const response = new Promise<string | boolean>((resolve) => {
		const modal: ModalSettings = {
			type: 'component',
			component: 'confirmModal',
			meta: { ...settings },
			response: (result: string | boolean) => {
				resolve(result)
			}
		}

		modalStore.trigger(modal)
	})

	return await response
}
