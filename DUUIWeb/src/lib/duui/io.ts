import { equals } from '$lib/utils/text'

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
	Text = 'Text'
}

export enum Output {
	Dropbox = 'Dropbox',
	Minio = 'Minio',
	Text = 'Text',
	None = 'None'
}

export const InputSources: string[] = ['Dropbox', 'Minio', 'Text'] // Option to upload files first?

export const InputFileExtensions: string[] = ['.txt', '.xmi', '.json', '.gz']

export const OutputTargets: string[] = ['Dropbox', 'Minio', 'None'] // File

export const OutputFileExtensions: string[] = ['.txt', '.xmi']

export const isCloudProvider = (provider: string) => {
	return equals(provider, 'dropbox') || equals(provider, 'minio')
}

export const isValidIO = (input: DUUIDocumentInput, output: DUUIDocumentOutput) => {
	return isValidInput(input) && isValidOutput(output)
}

export const isValidInput = (input: DUUIDocumentInput) => {
	if (equals(input.source, Input.Text)) {
		return input.content.length > 0
	}

	if (equals(input.source, Input.Minio)) {
		return isValidS3BucketName(input.folder)
	}

	if (equals(input.source, Input.Dropbox)) {
		return input.folder.length > 0
	}

	return true
}

export const isValidOutput = (output: DUUIDocumentOutput) => {
	if (equals(output.target, Output.Minio)) {
		return isValidS3BucketName(output.folder)
	}

	if (equals(output.target, Output.Dropbox)) {
		return output.folder.length > 0
	}

	return true
}

export const isValidS3BucketName = (bucket: string) => {
	if (bucket.length < 3 || bucket.length > 63) return false

	// Check valid characters and starting/ending with a letter or number
	if (!/^[a-zA-Z0-9]/.test(bucket) || !/[a-zA-Z0-9]$/.test(bucket)) {
		return false
	}

	// Check for valid characters (lowercase letters, numbers, dots, hyphens)
	if (!/^[a-z0-9.-]+$/.test(bucket)) {
		return false
	}

	// Check for adjacent periods
	if (/\.\./.test(bucket)) {
		return false
	}

	// Check for IP address format
	if (/^\d+\.\d+\.\d+\.\d+$/.test(bucket)) {
		return false
	}

	// Check for prefix xn--
	if (bucket.startsWith('xn--')) {
		return false
	}

	// Check for prohibited prefixes
	if (bucket.startsWith('sthree-') || bucket.startsWith('sthree-configurator')) {
		return false
	}

	// Check for reserved suffixes
	if (bucket.endsWith('-s3alias') || bucket.endsWith('--ol-s3')) {
		return false
	}

	return true
}

export const getTotalDuration = (document: DUUIDocument) => {
	return (
		document.decodeDuration +
		document.deserializeDuration +
		document.waitDuration +
		document.processDuration
	)
}
