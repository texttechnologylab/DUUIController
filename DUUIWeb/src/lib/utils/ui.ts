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
	faX
} from '@fortawesome/free-solid-svg-icons'
import { equals } from './text'
import { Status } from '$lib/duui/monitor'
import type { DUUIDocument } from '$lib/duui/io'
import type { ToastSettings } from '@skeletonlabs/skeleton'

export const documentStatusNames = [
	Status.Any,
	Status.Setup,
	Status.Input,
	Status.Decode,
	Status.Deserialize,
	Status.Waiting,
	Status.Running,
	Status.Output,
	Status.Shutdown,
	Status.Completed,
	Status.Canceled,
	Status.Failed
]

export const documentStatusNamesString = [
	'Any',
	'Setup',
	'Input',
	'Decode',
	'Deserialize',
	'Waiting',
	'Running',
	'Shutdown',
	'Output',
	'Completed',
	'Failed',
	'Canceled',
	'Unknown'
]

export function getStatusIcon(status: string) {
	if (equals(status, Status.Input)) return faFileDownload
	if (equals(status, Status.Setup)) return faArrowTrendUp
	if (equals(status, Status.Running)) return faRefresh
	if (equals(status, Status.Shutdown)) return faArrowTrendDown
	if (equals(status, Status.Output)) return faFileUpload
	if (equals(status, Status.Completed)) return faCheckDouble
	if (equals(status, Status.Canceled)) return faCancel
	if (equals(status, Status.Failed)) return faX

	return faQuestion
}

export const getDocumentStatusIcon = (document: DUUIDocument) => {
	if (equals(document.status, Status.Setup)) return faArrowTrendUp
	if (equals(document.status, Status.Input)) return faFileDownload
	if (equals(document.status, Status.Decode)) return faFileCode
	if (equals(document.status, Status.Deserialize)) return faFileArchive
	if (equals(document.status, Status.Waiting)) return faHourglass
	if (equals(document.status, Status.Canceled)) return document.error ? faX : faCancel
	if (equals(document.status, Status.Output)) return document.error ? faX : faFileUpload
	if (equals(document.status, Status.Failed)) return document.error ? faX : faCheck
	if (equals(document.status, Status.Completed)) return document.error ? faX : faCheckDouble

	return document.error ? faX : document.finished ? faCheck : faRefresh
}

export const success = (message: string, duration: number = 4000): ToastSettings => {
	return {
		message: message,
		timeout: duration,
		background: 'variant-filled-success'
	}
}

export const info = (message: string, duration: number = 4000): ToastSettings => {
	return {
		message: message,
		timeout: duration,
		background: 'variant-filled-surface'
	}
}
