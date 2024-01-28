import { type ModalStore, type ModalSettings, getModalStore } from '@skeletonlabs/skeleton'

// See +layout.svelte file for modalRegistry
export type ModalComponent = 'deleteModal' | 'documentModal' | 'promptModal' | 'confirmModal'

/**
 *
 * @param title The modal title
 * @param message The main message in the modal
 * @param modalComponent A Svelte Component that has been registered through the modalRegistry constant in the root layout (+layout.svelte)
 * @param modalStore The Svelte Writable store returned from getModalStore()
 * @returns
 */
export const showModal = async (
	settings: ModalSettings['meta'],
	modalComponent: ModalComponent,
	modalStore: ModalStore
) => {
	const response = new Promise<string | boolean>((resolve) => {
		const modal: ModalSettings = {
			type: 'component',
			component: modalComponent,
			meta: { ...settings },
			response: (result: string | boolean) => {
				resolve(result)
			}
		}

		modalStore.trigger(modal)
	})

	return await response
}
