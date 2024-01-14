import { includes } from '$lib/duui/utils/text'
import type { DUUIPipeline } from './pipeline'

export interface DUUIStatusEvent {
	oid: string
	process_id: string
	timestamp: number
	sender: string
	message: string
}

export const statusNames: string[] = [
	'Active',
	'Any',
	'Cancelled',
	'Completed',
	'Decode',
	'Deserialize',
	'Download',
	'Failed',
	'Idle',
	'Inactive',
	'Input',
	'Instatiating',
	'Output',
	'Setup',
	'Shutdown',
	'Starting',
	'Skipped',
	'Unknow',
	'Waiting'
]

export enum Status {
	Active = 'Active',
	Any = 'Any',
	Cancelled = 'Cancelled',
	Completed = 'Completed',
	Decode = 'Decode',
	Deserialize = 'Deserialize',
	Download = 'Download',
	Failed = 'Failed',
	Idle = 'Idle',
	Inactive = 'Inactive',
	Input = 'Input',
	Instantiating = 'Instatiating',
	Output = 'Output',
	Setup = 'Setup',
	Shutdown = 'Shutdown',
	Skipped = 'Skipped',
	ImageStart = 'Starting',
	Unknown = 'Unknow',
	Waiting = 'Waiting'
}

export const activeStatusList: string[] = ['Setup', 'Input', 'Active', 'Shutdown', 'Output']

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

export const getDocumentProgress = (
	documentProgress: Map<string, number>,
	pipeline: DUUIPipeline,
	document: string
) => {
	let progress = Math.max(0, documentProgress.get(document.split('/').at(-1) || '') || 0)
	return `${Math.min(progress, pipeline.components.length)} / ${pipeline.components.length}`
}
