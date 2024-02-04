import { equals } from '$lib/duui/utils/text'
import { IO, type DUUIDocumentProvider } from './io'

export interface DUUIProcess {
	oid: string
	pipeline_id: string
	status: string
	error: string
	progress: number
	started_at: number
	finished_at: number
	input: DUUIDocumentProvider
	output: DUUIDocumentProvider
	size: number
	settings: {
		notify: boolean
		check_target: boolean
		recursive: boolean
		overwrite: boolean
		sort_by_size: boolean
		minimum_size: number
		worker_count: number
		ignore_errors: boolean
	}
	document_names: string[]
	is_finished: boolean
	duration_setup: number
	duration_instantiation: number
	count: number
	pipeline_status: Map<string, string>
	initial: number
	skipped: number
}

export const processToSeachParams = (process: DUUIProcess) => {
	return `
		&input-provider=${process.input.provider}
		&input-path=${process.input.path}
		&input-content=${equals(process.input.provider, IO.Text) ? process.input.content : ''}
		&input-file-extension=${process.input.file_extension}
		&output-provider=${process.output.provider}
		&output-path=${process.output.path}
		&output-file-extension=${process.output.file_extension}
		&notify=${process.settings.notify || 'false'}
		&check_target=${process.settings.check_target || 'false'}
		&recursive=${process.settings.recursive || 'true'}
		&overwrite=${process.settings.overwrite || 'false'}
		&sort_by_size=${process.settings.sort_by_size || 'false'}
		&minimum_size=${process.settings.minimum_size || '0'}
		&worker_count=${process.settings.worker_count || '1'}
		&ignore_errors=${process.settings.ignore_errors || 'false'}
		`
}
