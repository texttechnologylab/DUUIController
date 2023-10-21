import { equals } from '$lib/utils/text'
import type { DUUIDocument, DUUIDocumentInput, DUUIDocumentOutput } from './io'
import type { DUUIStatusEvent } from './monitor'
import type { DUUIPipeline } from './pipeline'

export interface DUUIProcess {
	id: string
	status: string
	progress: number
	startedAt?: number
	finishedAt?: number
	input: DUUIDocumentInput
	output: DUUIDocumentOutput
	options: Map<string, string>
	pipeline_id: string
	log: DUUIStatusEvent[]
	done: boolean
	documentCount: number
	documentNames: string[]
	documents: DUUIDocument[]
}

export const progressMaximum = (process: DUUIProcess, pipeline: DUUIPipeline) => {
	return equals(process.input.source, 'text') ? pipeline.components.length : process.documentCount
}

export const processAsSeachParams = (process: DUUIProcess) => {
	return `
		input-source=${process.input.source}&
		input-folder=${process.input.folder}&
		input-content=${process.input.content}&
		input-file-extension=${process.input.fileExtension}&
		output-target=${process.output.target}&
		output-folder=${process.output.folder}&
		output-file-extension=${process.output.fileExtension}&`
}
