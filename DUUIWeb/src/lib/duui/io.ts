import { equals } from '$lib/duui/utils/text'

export interface DUUIDocument {
	name: string
	path: string
	status: string
	progress: number
	finished: boolean
	error: string
	decodeDuration: number
	deserializeDuration: number
	waitDuration: number
	processDuration: number
	size: number
	startTime: number
	endTime: number
	annotations: {}
}

export interface DUUIDocumentInput {
	source: string
	folder: string
	content: string
	fileExtension: string
}

export interface DUUIDocumentOutput {
	target: string
	folder: string
	fileExtension: string
}

export enum Input {
	Dropbox = 'Dropbox',
	Minio = 'Minio',
	Text = 'Text',
	LocalFile = 'Local File'
}

export enum Output {
	Dropbox = 'Dropbox',
	Minio = 'Minio',
	LocalFile = 'Local File',
	None = 'None'
}

export const InputSources: string[] = ['Dropbox', 'Local File', 'Minio', 'Text']

export const InputFileExtensions: string[] = ['.txt', '.xmi', '.gz']

export const OutputTargets: string[] = ['Dropbox', 'Local File', 'Minio', 'None']

export const OutputFileExtensions: string[] = ['.txt', '.xmi']

export const isCloudProvider = (provider: string) => {
	return equals(provider, 'dropbox') || equals(provider, 'minio')
}

export const isValidIO = (
	input: DUUIDocumentInput,
	output: DUUIDocumentOutput,
	files: FileList
) => {
	return isValidInput(input, files) && isValidOutput(output)
}

export const isValidInput = (input: DUUIDocumentInput, files: FileList) => {
	if (equals(input.source, Input.Text)) {
		return input.content.length > 0
	}

	if (equals(input.source, Input.LocalFile)) {
		return files?.length > 0
	}

	if (equals(input.source, Input.Minio)) {
		return isValidS3BucketName(input.folder).length === 0
	}

	if (equals(input.source, Input.Dropbox)) {
		return input.folder.length > 0 && input.folder.startsWith('/')
	}

	return true
}

export const isValidOutput = (output: DUUIDocumentOutput) => {
	if (equals(output.target, Output.Minio)) {
		return isValidS3BucketName(output.folder).length === 0
	}

	if (equals(output.target, Output.Dropbox)) {
		return output.folder.length > 0
	}

	return true
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
		document.decodeDuration +
		document.deserializeDuration +
		document.waitDuration +
		document.processDuration
	)
}
