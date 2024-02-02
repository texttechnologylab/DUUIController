import { type ModalStore, type ModalSettings, getModalStore } from '@skeletonlabs/skeleton'

// See +layout.svelte file for modalRegistry
export type ModalComponent = 'deleteModal' | 'documentModal' | 'promptModal' | 'confirmModal'

/**
 *
 * @param title The modal title
 * @param message The main message in the modal
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