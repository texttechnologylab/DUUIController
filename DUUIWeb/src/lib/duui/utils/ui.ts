import type { DUUIDocument } from '$lib/duui/io'
import { Status } from '$lib/duui/monitor'
import {
	faArrowTrendDown,
	faArrowTrendUp,
	faCancel,
	faCheck,
	faCheckDouble,
	faClose,
	faFileArchive,
	faFileCode,
	faFileDownload,
	faFileUpload,
	faHourglass,
	faQuestion,
	faRefresh,
	faSlash,
	faWarning
} from '@fortawesome/free-solid-svg-icons'
import type { ToastSettings } from '@skeletonlabs/skeleton'
import { equals } from './text'

export const documentStatusNames = [
	Status.Any,
	Status.Active,
	Status.Cancelled,
	Status.Completed,
	Status.Decode,
	Status.Deserialize,
	Status.Failed,
	Status.Input,
	Status.Output,
	Status.Setup,
	Status.Shutdown,
	Status.Waiting
]

export const documentStatusNamesString = [
	'Active',
	'Any',
	'Cancelled',
	'Completed',
	'Decode',
	'Deserialize',
	'Failed',
	'Input',
	'Output',
	'Setup',
	'Shutdown',
	'Unknown',
	'Waiting'
]

/**
 * Select the appropriate icon for the given status.
 * @param status
 * @returns An IconDefinition according to svelte-fa
 */
export function getStatusIcon(status: string) {
	if (equals(status, Status.Input)) return faFileDownload
	if (equals(status, Status.Setup)) return faArrowTrendUp
	if (equals(status, Status.Active)) return faRefresh
	if (equals(status, Status.Shutdown)) return faArrowTrendDown
	if (equals(status, Status.Output)) return faFileUpload
	if (equals(status, Status.Completed)) return faCheckDouble
	if (equals(status, Status.Cancelled)) return faCancel
	if (equals(status, Status.Failed)) return faWarning

	return faQuestion
}

/**
 * Select the appropriate icon for the given document status.
 * @param document
 * @returns An IconDefinition according to svelte-fa
 */
export const getDocumentStatusIcon = (document: DUUIDocument) => {
	if (equals(document.status, Status.Setup)) return faArrowTrendUp
	if (equals(document.status, Status.Input)) return faFileDownload
	if (equals(document.status, Status.Decode)) return faFileCode
	if (equals(document.status, Status.Deserialize)) return faFileArchive
	if (equals(document.status, Status.Skipped)) return faSlash
	if (equals(document.status, Status.Waiting)) return faHourglass
	if (equals(document.status, Status.Inactive)) return faHourglass
	if (equals(document.status, Status.Cancelled)) return document.error ? faClose : faCancel
	if (equals(document.status, Status.Output)) return document.error ? faClose : faFileUpload
	if (equals(document.status, Status.Failed)) return faWarning
	if (equals(document.status, Status.Completed)) return document.error ? faClose : faCheckDouble

	return document.error ? faClose : document.is_finished ? faCheck : faRefresh
}

/**
 * Create an object for a success Toast
 * @param message The message to display
 * @param duration The duration to keep the toast visible
 * @returns Settings describing the toast.
 */
export const successToast = (message: string, duration: number = 4000): ToastSettings => {
	return {
		message: message,
		timeout: duration,
		classes: 'toast-success',
		hideDismiss: true
	}
}

/**
 * Create an object for an informative Toast
 * @param message The message to display
 * @param duration The duration to keep the toast visible
 * @returns Settings describing the toast.
 */
export const infoToast = (message: string, duration: number = 4000): ToastSettings => {
	return {
		message: message,
		timeout: duration,
		classes: 'toast-info',
		hideDismiss: true
	}
}

/**
 * Create an object for an error Toast
 * @param message The message to display
 * @param duration The duration to keep the toast visible
 * @returns Settings describing the toast.
 */
export const errorToast = (message: string, duration: number = 4000): ToastSettings => {
	return {
		message: message,
		timeout: duration,
		classes: 'toast-error',
		hideDismiss: true
	}
}

/**
 * Scroll the element with the specified id into view.
 * @param id The id tag of the HTMLElement
 */
export const scrollIntoView = (id: string) => {
	const el = document.querySelector(`#${id}`)
	if (!el) return

	el.scrollIntoView()
}
