import { includes } from '$lib/utils/text'
import type { DUUIComponent } from './component'
import type { DUUIPipeline } from './pipeline'

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
	Any = 'Any',
	Setup = 'Setup',
	Input = 'Input',
	Decode = 'Decode',
	Deserialize = 'Deserialize',
	Waiting = 'Waiting',
	Running = 'Running',
	Shutdown = 'Shutdown',
	Output = 'Output',
	Completed = 'Completed',
	Failed = 'Failed',
	Canceled = 'Canceled',
	Unknown = 'Unknown'
}

export const activeStatusList: string[] = ['Setup', 'Input', 'Running', 'Shutdown', 'Output']
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

export const getComponentStatus = (log: DUUIStatusEvent[], component: DUUIComponent) => {
	for (let event of log) {
		if (includes(event.message, 'Shutting down component ' + component.name)) return 'Shutdown'
		if (includes(event.message, 'Finished setup for component ' + component.name)) return 'Active'
		if (includes(event.message, 'Instantiating component ' + component.name)) return 'Instantiating'
		if (includes(event.message, 'Added component ' + component.name)) return 'Added'
	}
	return 'Setup'
}
