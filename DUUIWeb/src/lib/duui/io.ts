import { equals } from '$lib/duui/utils/text'
import type { DUUIEvent } from './monitor'

export interface DUUIDocument {
	oid: string
	name: string
	path: string
	status: string
	progress: number
	is_finished: boolean
	error: string | null
	duration_decode: number
	duration_deserialize: number
	duration_wait: number
	duration_process: number
	duration: number
	size: number
	started_at: number
	finished_at: number
	annotations: {
		[key: string]: number
	}
	events: DUUIEvent[]
}

export type DUUIDocumentProvider = {
	provider: IOProvider
	path: string
	content: string
	fileExtension: FileExtension
}

export type IOProvider = 'Dropbox' | 'Minio' | 'File' | 'Text' | 'None'
export type FileExtension = '.txt' | '.xmi' | '.gz'

export enum IO {
	Dropbox = 'Dropbox',
	File = 'File',
	Minio = 'Minio',
	Text = 'Text',
	None = 'None'
}

export const InputSources: string[] = ['Dropbox', 'File', 'Minio', 'Text']
export const InputFileExtensions: string[] = ['.txt', '.xmi', '.gz']

export const OutputTargets: string[] = ['Dropbox', 'Minio', 'None']
export const OutputFileExtensions: string[] = ['.txt', '.xmi']

export const isCloudProvider = (provider: string) => {
	return equals(provider, 'dropbox') || equals(provider, 'minio')
}

export const isValidIO = (
	input: DUUIDocumentProvider,
	output: DUUIDocumentProvider,
	files: FileList
): boolean => {
	return isValidInput(input, files) && isValidOutput(output)
}

/**
 * Check if the input settings are valid.
 *
 * @param input The input settings for the process.
 * @param files The list of files that have to be uploaded as the data source.
 * @returns whether the input settings are valid.
 */
export const isValidInput = (input: DUUIDocumentProvider, files: FileList): boolean => {
	if (equals(input.provider, IO.Text)) {
		return !!input.content && input.content.length > 0
	}

	if (equals(input.provider, IO.File)) {
		return files?.length > 0
	}

	if (equals(input.provider, IO.Minio)) {
		return isValidS3BucketName(input.path || '').length === 0
	}

	if (equals(input.provider, IO.Dropbox) && input.path === '/') return false

	return true
}

/**
 * Check if the output settings are valid.
 *
 * @param output The output settings for the process.
 * @returns whether the output settings are valid.
 */
export const isValidOutput = (output: DUUIDocumentProvider): boolean => {
	if (equals(output.provider, IO.Minio)) {
		return isValidS3BucketName(output.path || '').length === 0
	}
	if (equals(output.provider, IO.Dropbox) && output.path === '/') return false

	return true
}

/**
 * Check if the process settings are valid.
 *
 * @param workerCount The maximum number of threads usable by the composer.
 * @param minimumSize The minimum file size in bytes.
 * @returns whether the settings are valid.
 */
export const areSettingsValid = (workerCount: number, minimumSize: number): string => {
	if (workerCount < 1 || workerCount > 20) {
		return 'Worker count must be between 1 and 20'
	}

	if (minimumSize < 0 || minimumSize > 2147483647) {
		return 'File size must be between 0 and 2147483647 bytes'
	}

	return ''
}

/**
 * Given a path as a string, check if the bucket name component of the path is valid. The bucket is expected to be
 * the part before the first forward slash or, if non exists the entire path.
 *
 * @param path The path to check
 * @returns
 */
export const isValidS3BucketName = (path: string): string => {
	if (path.startsWith('/')) return 'Bucket name must not begin with a forward slash'
	if (path.endsWith('/')) return 'Path name must not end with a forward slash'

	let bucket: string = ''

	if (!path.includes('/')) {
		bucket = path
	} else {
		const parts: string[] = path.split('/')
		if (parts.length === 1) return 'Bucket name can not be empty'
		bucket = parts.at(0) as string
	}

	if (bucket.length < 3 || bucket.length > 63)
		return 'Bucket name must be between 3 (min) and 63 (max) characters long'

	// Check valid characters and starting/ending with a letter or number
	if (!/^[a-zA-Z0-9]/.test(bucket) || !/[a-zA-Z0-9]$/.test(bucket)) {
		return 'Bucket name must begin and end with a letter or number'
	}

	// Check for valid characters (lowercase letters, numbers, dots, hyphens)
	if (!/^[a-z0-9.-]+$/.test(bucket)) {
		return 'Bucket name can consist only of lowercase letters, numbers, dots (.), and hyphens (-)'
	}

	// Check for adjacent periods
	if (/\.\./.test(bucket)) {
		return 'Bucket name must not contain two adjacent periods'
	}

	// Check for IP address format
	if (/^\d+\.\d+\.\d+\.\d+$/.test(bucket)) {
		return 'Bucket name must not be formatted as an IP address (for example, 192.168.5.4)'
	}

	// Check for prefix xn--
	if (bucket.startsWith('xn--')) {
		return 'Bucket name must not start with the prefix xn--'
	}

	// Check for prohibited prefixes
	if (bucket.startsWith('sthree-') || bucket.startsWith('sthree-configurator')) {
		return 'Bucket name must not start with the prefix sthree- or sthree-configurator'
	}

	// Check for reserved suffixes
	if (bucket.endsWith('-s3alias') || bucket.endsWith('--ol-s3')) {
		return 'Bucket name must not end with the suffix -s3alias or --ol-ss3'
	}

	return ''
}

/**
 * Helper function to sum the duration of all steps for a document.
 *
 * @param document a Document that is being or has been processed.
 * @returns the total duration in the pipeline.
 */
export const getTotalDuration = (document: DUUIDocument) => {
	return (
		document.duration_decode +
		document.duration_deserialize +
		document.duration_wait +
		document.duration_process
	)
}
