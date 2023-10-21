import {
	faCancel,
	faCheck,
	faCheckDouble,
	faClock,
	faFileDownload,
	faFileUpload,
	faGears,
	faQuestion,
	faRefresh,
	faX
} from '@fortawesome/free-solid-svg-icons'
import { equals } from './text'
import { Status, isActive } from '$lib/duui/monitor'
import type { DUUIDocument } from '$lib/duui/io'

export function getStatusIcon(status: string) {
	if (equals(status, 'input')) return faFileDownload
	if (equals(status, 'setup')) return faGears
	if (equals(status, 'running')) return faRefresh
	if (equals(status, 'shutdown')) return faClock
	if (equals(status, 'output')) return faFileUpload
	if (equals(status, 'completed')) return faCheckDouble
	if (equals(status, 'canceled')) return faCancel
	if (equals(status, 'failed')) return faX

	return faQuestion
}

export const getDocumentStatusIcon = (
	document: DUUIDocument,
	status: string,
	inputIsText: boolean
) => {
	if (document.done) return faCheckDouble

	if (equals(status, Status.Running)) {
		return document.error ? faX : faRefresh
	}

	if (inputIsText && !document.error) return faCheckDouble
	return document.done ? faCheckDouble : faFileDownload
}
