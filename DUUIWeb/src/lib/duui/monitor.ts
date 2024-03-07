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

export const ACTIVE_STATUS_LIST: string[] = ['Setup', 'Input', 'Active', 'Shutdown', 'Output']

/**
 * Check if the status parameters is part of the ACTIVE_STATUS_LIST.
 * 
 * @param status The status of the process or document.
 * @returns if the status represents an active state.
 */
export const isActive = (status: string) => {
	return ACTIVE_STATUS_LIST.includes(status)
}
