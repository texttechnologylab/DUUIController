import { includes } from '$lib/duui/utils/text'
import type { DUUIPipeline } from './pipeline'

export interface DUUIEvent {
	timestamp: number
	event: {
		process_id: string
		sender: string
		message: string
	}
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
	'Unknown',
	'Waiting'
]

export const PROCESS_STATUS_NAMES: string[] = [
	'Any',
	'Setup',
	'Input',
	'Active',
	'Output',
	'Completed',
	'Cancelled',
	'Failed'
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
	Unknown = 'Unknown',
	Waiting = 'Waiting'
}

export const activeStatusList: string[] = ['Setup', 'Input', 'Active', 'Shutdown', 'Output']

export const isActive = (status: string) => {
	return activeStatusList.includes(status)
}

export const documentIsProcessed = (events: DUUIEvent[], document: string) => {
	const name = document.split('/').at(-1) || ''

	for (let item of events) {
		if (includes(item.event.message, name + ' has been processed')) {
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
