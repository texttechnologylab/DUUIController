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
import type { DUUIPipeline } from '$lib/duui/pipeline'
import type { DUUIProcess } from '$lib/duui/process'
import { isActive, isDocumentProcessed } from '$lib/duui/monitor'

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
	documentProgress: Map<string, number>,
	status: string,
	process: DUUIProcess,
	pipeline: DUUIPipeline,
	document: string
) => {
	const documentProcessed = isDocumentProcessed(documentProgress, process, pipeline, document)
	const active = isActive(status)

	if (!active) {
		if (documentProcessed) return faCheckDouble
		else return faX
	} else {
		if (equals(status, 'input')) return faFileDownload
		if (!documentProcessed) return faRefresh
		if (equals(status, 'output') && documentProcessed) return faFileUpload
		return faCheck
	}
}



