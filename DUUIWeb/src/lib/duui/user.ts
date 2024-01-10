import { equals } from '$lib/duui/utils/text'

export const needsAuthorization = (source: string, target: string): boolean => {
	return equals(source, 'dropbox') || equals(target, 'dropbox')
}
