import { equals } from '$lib/duui/utils/text'
import { Input, type DUUIDocumentProvider } from './io'
import type { DUUIPipeline } from './pipeline'

export interface DUUIProcess {
	oid: string
	pipeline_id: string
	status: string
	error: string
	progress: number
	startTime: number
	endTime: number
	input: DUUIDocumentProvider
	output: DUUIDocumentProvider
	settings: {
		notify: boolean
		checkTarget: boolean
		recursive: boolean
		overwrite: boolean
		sortBySize: boolean
		skipFiles: number
		workerCount: number
	}
	documentNames: string[]
	finished: boolean
	setupDuration: number
	instantiationDuration: number
	count: number
	pipelineStatus: Map<string, string>
}

export const progressMaximum = (process: DUUIProcess, pipeline: DUUIPipeline) => {
	return process.documentNames.length
}

export const processToSeachParams = (process: DUUIProcess) => {
	return `
		&input-provider=${process.input.provider}
		&input-path=${process.input.path}
		&input-content=${equals(process.input.provider, Input.Text) ? process.input.content : ''}
		&input-file-extension=${process.input.fileExtension}
		&output-provider=${process.output.provider}
		&output-path=${process.output.path}
		&output-file-extension=${process.output.fileExtension}
		&notify=${process.settings.notify || 'false'}
		&checkTarget=${process.settings.checkTarget || 'false'}
		&recursive=${process.settings.recursive || 'false'}
		&overwrite=${process.settings.overwrite || 'false'}
		&sortBySize=${process.settings.sortBySize || 'false'}
		&skipFiles=${process.settings.skipFiles || '0'}
		&workerCount=${process.settings.workerCount || '5'}
		`
}
