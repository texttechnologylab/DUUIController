import {
	faArrowTrendDown,
	faArrowTrendUp,
	faCancel,
	faCheck,
	faCheckDouble,
	faFileArchive,
	faFileCode,
	faFileDownload,
	faFileUpload,
	faHourglass,
	faQuestion,
	faRefresh,
	faClose,
	faWarning,
	faSlash
} from '@fortawesome/free-solid-svg-icons'
import { equals } from './text'
import { Status } from '$lib/duui/monitor'
import type { DUUIDocument } from '$lib/duui/io'
import type { ToastSettings } from '@skeletonlabs/skeleton'

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

export const getDocumentStatusIcon = (document: DUUIDocument) => {
	if (equals(document.status, Status.Setup)) return faArrowTrendUp
	if (equals(document.status, Status.Input)) return faFileDownload
	if (equals(document.status, Status.Decode)) return faFileCode
	if (equals(document.status, Status.Deserialize)) return faFileArchive
	if (equals(document.status, Status.Skipped)) return faSlash
	if (equals(document.status, Status.Waiting)) return faHourglass
	if (equals(document.status, Status.Cancelled)) return document.error ? faClose : faCancel
	if (equals(document.status, Status.Output)) return document.error ? faClose : faFileUpload
	if (equals(document.status, Status.Failed)) return document.error ? faWarning : faCheck
	if (equals(document.status, Status.Completed)) return document.error ? faClose : faCheckDouble

	return document.error ? faClose : document.finished ? faCheck : faRefresh
}

export const success = (message: string, duration: number = 4000): ToastSettings => {
	return {
		message: message,
		timeout: duration,
		classes:
			'border-2 bg-white dark:bg-surface-600 dark:text-surface-200 shadow-lg !rounded-none border-success-500'
	}
}

export const info = (message: string, duration: number = 4000): ToastSettings => {
	return {
		message: message,
		timeout: duration,
		classes:
			'border-2 bg-white dark:bg-surface-600 dark:text-surface-200 shadow-lg !rounded-none border-primary-500'
	}
}

export const error = (message: string, duration: number = 4000): ToastSettings => {
	return {
		message: message,
		timeout: duration,
		classes:
			'border-2 bg-white dark:bg-surface-600 dark:text-surface-200 shadow-lg !rounded-none border-error-500'
	}
}

export const scrollIntoView = (id: string) => {
	const el = document.querySelector(`#${id}`)
	if (!el) return

	el.scrollIntoView({
		behavior: 'smooth'
	})
}

export const variantPrimary: string = 'variant-filled-primary dark:variant-soft-primary'
export const variantGlassPrimary: string = 'variant-glass-primary'
export const variantError: string = 'variant-filled-error dark:variant-soft-error'
export const variantSuccess: string = 'variant-filled-success dark:variant-soft-success'
