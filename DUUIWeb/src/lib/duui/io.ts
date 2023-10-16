import { equals } from '$lib/utils/text'

export interface DUUIDocument {
	name: string
	path: string
	done: boolean
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

export const outputIsCloudProvider = (target: string) => {
	return equals(target, 'dropbox') || equals(target, 'minio')
}

export const isValidSourceAndTarget = (input: DUUIDocumentInput, output: DUUIDocumentOutput) => {
	return (
		(equals(input.source, 'Text') && !input.content) ||
		(outputIsCloudProvider(output.target) && !output.folder)
	)
}
