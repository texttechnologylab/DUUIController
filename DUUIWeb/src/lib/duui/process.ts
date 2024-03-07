import { equals } from '$lib/duui/utils/text'
import { IO, type DUUIDocumentProvider, type FileExtension } from './io'

export interface ProcessSettings {
	pipeline_id: string
	input: DUUIDocumentProvider
	output: DUUIDocumentProvider
	settings: {
		notify: boolean
		check_target: boolean
		recursive: boolean
		overwrite: boolean
		sort_by_size: boolean
		minimum_size: number
		worker_count: number
		ignore_errors: boolean
		language: string
	}
}

export interface DUUIProcess {
	oid: string
	pipeline_id: string
	status: string
	error: string
	progress: number
	started_at: number
	finished_at: number
	size: number
	input: DUUIDocumentProvider
	output: DUUIDocumentProvider
	settings: {
		notify: boolean
		check_target: boolean
		recursive: boolean
		overwrite: boolean
		sort_by_size: boolean
		minimum_size: number
		worker_count: number
		ignore_errors: boolean
		language: string
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

/**
 * Create and return an object with default settings for a process.
 *
 * @returns an object with default settings for a process.
 */
export const blankSettings = () => {
	return {
		pipeline_id: '',
		input: {
			provider: IO.Text,
			path: '',
			content: 'Sample Text.',
			file_extension: '.txt' as FileExtension
		},
		output: {
			provider: IO.None,
			path: '',
			content: '',
			file_extension: '.txt' as FileExtension
		},
		settings: {
			language: 'Unspecified',
			notify: false,
			check_target: false,
			recursive: true,
			overwrite: false,
			sort_by_size: false,
			ignore_errors: true,
			minimum_size: 0,
			worker_count: 1
		}
	}
}

/**
 * Convert a process into a string with URL search parameters.
 *
 * @param process The process to convert to a string of search params.
 * @returns a string with settings for the process as serach parameters.
 */
export const processToSeachParams = (process: DUUIProcess) => {
	return `
		&input_provider=${process.input.provider}
		&input_path=${process.input.path}
		&input_content=${equals(process.input.provider, IO.Text) ? process.input.content : ''}
		&input_file_extension=${process.input.file_extension}
		&output_provider=${process.output.provider}
		&output_path=${process.output.path}
		&output_file_extension=${process.output.file_extension}
		&notify=${process.settings.notify || 'false'}
		&check_target=${process.settings.check_target || 'false'}
		&recursive=${process.settings.recursive || 'true'}
		&overwrite=${process.settings.overwrite || 'false'}
		&sort_by_size=${process.settings.sort_by_size || 'false'}
		&minimum_size=${process.settings.minimum_size || '0'}
		&worker_count=${process.settings.worker_count || '1'}
		&ignore_errors=${process.settings.ignore_errors || 'false'}
		&language=${process.settings.language || 'Unspecified'}
		`
}

// Available Languages for processing
export type Language = 'Unspecified' | 'German' | 'English' | 'French'
export const Languages = ['Unspecified', 'German', 'English', 'French']
