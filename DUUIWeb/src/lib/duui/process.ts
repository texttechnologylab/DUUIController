import type { DUUIDocumentInput, DUUIDocumentOutput } from './io'
import type { DUUIPipeline } from './pipeline'

export interface DUUIProcess {
	oid: string
	pipeline_id: string
	status: string
	error: string
	progress: number
	startTime: number
	endTime: number
	input: DUUIDocumentInput
	output: DUUIDocumentOutput
	settings: Map<string, Object>
	documentNames: string[]
	finished: boolean
	setupDuration: number
	instantiationDuration: number
	count: number
}

export const progressMaximum = (process: DUUIProcess, pipeline: DUUIPipeline) => {
	return process.documentNames.length
}

export const processToSeachParams = (process: DUUIProcess) => {
	return `
		input-source=${process.input.source}&
		input-folder=${process.input.folder}&
		input-content=${process.input.content}&
		input-file-extension=${process.input.fileExtension}&
		output-target=${process.output.target}&
		output-folder=${process.output.folder}&
		output-file-extension=${process.output.fileExtension}&`
}
