import { includes } from '$lib/utils/text'
import { outputIsCloudProvider } from './io'
import type { DUUIPipeline } from './pipeline'
import type { DUUIProcess } from './process'

export interface DUUIStatusEvent {
	timestamp: number
	sender: string
	message: string
}

export const statusNames: string[] = [
	'Input',
	'Setup',
	'Running',
	'Shutdown',
	'Output',
	'Completed',
	'Failed',
	'Canceled',
	'Unknown'
]

export enum Status {
	Input = 'Input',
	Setup = 'Setup',
	Running = 'Running',
	Shutdown = 'Shutdown',
	Output = 'Output',
	Completed = 'Completed',
	Failed = 'Failed',
	Canceled = 'Canceled',
	Unknown = 'Unknown'
}

export const activeStatusList: string[] = ['Input', 'Setup', 'Running', 'Shutdown', 'Output']
export const isActive = (status: string) => {
	return activeStatusList.includes(status)
}

export const documentIsProcessed = (log: DUUIStatusEvent[], document: string) => {
	const name = document.split('/').at(-1) || ''

	for (let event of log) {
		if (includes(event.message, name + ' has been processed')) {
			return true
		}
	}
	return false
}

export const isDocumentProcessed = (
	documentProgress: Map<string, number>,
	process: DUUIProcess,
	pipeline: DUUIPipeline,
	document: string
) => {
	if (outputIsCloudProvider(process.output.target)) {
		return documentProgress.get(document.split('/').at(-1) || '') === pipeline.components.length + 1
	} else {
		return documentProgress.get(document.split('/').at(-1) || '') === pipeline.components.length
	}
}

export const getDocumentProgress = (
	documentProgress: Map<string, number>,
	pipeline: DUUIPipeline,
	document: string
) => {
	let progress = Math.max(0, documentProgress.get(document.split('/').at(-1) || '') || 0)
	return `${Math.min(progress, pipeline.components.length)} / ${pipeline.components.length}`
}


