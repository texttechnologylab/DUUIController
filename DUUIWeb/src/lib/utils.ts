import type { DUUIProcess } from './data'

export const slugify = (text: string) =>
	text
		.toLowerCase()
		.trim()
		.replace(/[^\w\s-]/g, '')
		.replace(/[\s_-]+/g, '-')
		.replace(/^-+|-+$/g, '')

export const toTitleCase = (text: string) => <string>text[0].toUpperCase() + text.slice(1)
export const toDateTimeString = (date: Date) => <string>''

export const getDuration = (process: DUUIProcess) => {
	if (!process.startedAt) {
		return 0
	}

	if (!process.finishedAt) {
		return new Date(Date.now() - process.startedAt).getSeconds()
	}

	return new Date(process.finishedAt - process.startedAt).getSeconds()
}

const urlRegex =
	'https?://(www.)?[-a-zA-Z0-9@:%._+~#=]{1,256}.[a-zA-Z0-9()]{1,6}\b([-a-zA-Z0-9()@:%_+.~#?&//=]*)'

const regex = new RegExp(urlRegex)

export const validateURL = (url: string) => {
	if (url.match(regex)) return true
	return false
}

export const handleLoginRedirect = (
	url: URL,
	message: string = 'You must be logged in to access this ressource.'
) => {
	return `/user/login?redirectTo=${url.pathname + url.search}&message=${message}`
}

export const formatFileSize = (bytes: number, decimals: number = 0) => {
	if (!bytes) {
		return '0 B'
	}

	const factor = 1024
	const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
	const size = Math.floor(Math.log(bytes) / Math.log(factor))

	return `${parseFloat((bytes / Math.pow(factor, size)).toFixed(decimals))} ${sizes[size]}`
}

export const cutText = (text: string, maxSize: number = 50) => {
	let newText: string = text.slice(0, 50)
	if (newText !== text) {
		return newText + '...'
	}

	return newText
}
