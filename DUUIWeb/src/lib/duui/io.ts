import { DropboxAppURL } from '$lib/config'
import { equals } from '$lib/duui/utils/text'

export interface DUUIDocument {
	name: string
	path: string
	status: string
	progress: number
	finished: boolean
	error: string
	duration_decode: number
	duration_deserialize: number
	duration_wait: number
	duration_process: number
	duration_total: number | undefined
	size: number
	started_at: number
	finished_at: number
	annotations: {
		string: number
	}
}

export type DUUIDocumentProvider = {
	provider: IOProvider
	path: string
	content: string
	fileExtension: FileExtension
}

export type IOProvider = 'Dropbox' | 'Minio' | 'MongoDB' | 'File' | 'Text' | 'None'
export type FileExtension = '.txt' | '.xmi' | '.gz' | ''

export enum Input {
	Dropbox = 'Dropbox',
	Minio = 'Minio',
	MongoDB = 'MongoDB',
	Text = 'Text',
	File = 'File'
}

export enum Output {
	Dropbox = 'Dropbox',
	Minio = 'Minio',
	MongoDB = 'MongoDB',
	File = 'File',
	None = 'None'
}

export enum IO {
	Dropbox = 'Dropbox',
	File = 'File',
	Minio = 'Minio',
	MongoDB = 'MongoDB',
	Text = 'Text'
}

export const InputSources: string[] = ['Dropbox', 'File', 'Minio', 'MongoDB', 'Text']

export const InputFileExtensions: string[] = ['.txt', '.xmi', '.gz']

export const OutputTargets: string[] = ['Dropbox', 'File', 'Minio', 'MongoDB', 'None']

export const OutputFileExtensions: string[] = ['.txt', '.xmi']

export const isCloudProvider = (provider: string) => {
	return equals(provider, 'dropbox') || equals(provider, 'minio')
}

export const isValidIO = (
	input: DUUIDocumentProvider,
	output: DUUIDocumentProvider,
	files: FileList
) => {
	return isValidInput(input, files) && isValidOutput(output)
}

export const isValidInput = (input: DUUIDocumentProvider, files: FileList) => {
	if (equals(input.provider, Input.Text)) {
		return input.content && input.content.length > 0
	}

	if (equals(input.provider, Input.File)) {
		return files?.length > 0
	}

	if (equals(input.provider, Input.Minio)) {
		return isValidS3BucketName(input.path || '').length === 0
	}

	if (equals(input.provider, Input.Dropbox)) {
		return input.path && input.path.length > 0 && input.path.startsWith('/')
	}

	return true
}

export const isValidOutput = (output: DUUIDocumentProvider) => {
	if (equals(output.provider, Output.Minio)) {
		return isValidS3BucketName(output.path || '').length === 0
	}

	if (equals(output.provider, Output.Dropbox)) {
		return output.path && output.path.length > 0
	}

	return true
}

export const areSettingsValid = (workerCount: number, size: number) => {
	if (workerCount < 1 || workerCount > 20) {
		return 'Worker count must be between 1 and 20'
	}

	if (size < 0 || size > 2147483647) {
		return 'File size must be between 0 and 2147483647 bytes'
	}

	return ''
}

export const isValidS3BucketName = (bucket: string) => {
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

export const getTotalDuration = (document: DUUIDocument) => {
	return (
		document.duration_decode +
		document.duration_deserialize +
		document.duration_wait +
		document.duration_process
	)
}

export const URLFromProvider = (provider: DUUIDocumentProvider) => {
	if (provider.provider === 'Dropbox') return DropboxAppURL
	return ''
}
