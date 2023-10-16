import { equals } from '$lib/utils/text'

export const needsAuthorization = (source: string, target: string): boolean => {
	return equals(source, 'dropbox') || equals(source, 'dropbox')
}
