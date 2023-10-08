import {
	faCancel,
	faCheck,
	faExclamation,
	faFileArrowUp,
	faQuestion,
	faRotate,
	faWrench,
	faX
} from '@fortawesome/free-solid-svg-icons'
import {
	DUUIStatus,
	type DUUIProcess,
	type DUUIStatusEvent,
	type DUUIPipeline,
	DUUIDocumentSource,
	DUUIDocumentOutput
} from './data'

export const slugify = (text: string) =>
	text
		.toLowerCase()
		.trim()
		.replace(/[^\w\s-]/g, '')
		.replace(/[\s_-]+/g, '-')
		.replace(/^-+|-+$/g, '')

export const toTitleCase = (text: string) => <string>text[0].toUpperCase() + text.slice(1)
export function toDateTimeString(date: Date) {
	const options: Intl.DateTimeFormatOptions = {
		year: 'numeric',
		month: '2-digit',
		day: '2-digit',
		hour: '2-digit',
		minute: '2-digit'
	}
	return date.toLocaleString('de-DE', options)
}

export const getDuration = (process: DUUIProcess) => {
	if (process.startedAt === null || process.startedAt === undefined) {
		return 0
	}

	if (process.finishedAt === null || process.finishedAt === undefined) {
		return Math.ceil((Date.now() - process.startedAt) / 1000)
	}

	return Math.ceil((process.finishedAt - process.startedAt) / 1000)
}

const formatSeconds = (durationInSeconds: number) => {
	const hours = Math.floor(durationInSeconds / 3600)
	const minutes = Math.floor((durationInSeconds % 3600) / 60)
	const seconds = Math.floor(durationInSeconds % 60)

	const hoursText = hours > 0 ? `${hours}h` : ''
	const minutesText = minutes > 0 ? `${minutes}m` : ''
	const secondsText = seconds > 0 && hours <= 0 ? `${seconds}s` : ''

	const result = [hoursText, minutesText, secondsText].filter(Boolean).join(' ')

	return result || '0s'
}

export const getTimeDifference = (start: number | undefined, end: number | undefined) => {
	if (!start) {
		return 0
	}

	if (!end) {
		return formatSeconds(Math.ceil((Date.now() - start) / 1000))
	}

	return formatSeconds(Math.ceil((end - start) / 1000))
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

export function pipelineActive(status: string) {
	return [
		DUUIStatus.Setup.valueOf(),
		DUUIStatus.Running.valueOf(),
		DUUIStatus.Output.valueOf()
	].includes(status)
}

export function documentIsProcessed(log: DUUIStatusEvent[], document: string): boolean {
	const name: string | undefined = document.split('/').at(-1)

	for (let statusEvent of log) {
		if (name && statusEvent.message.includes(name)) {
			return true
		}
	}
	return false
}

export function getIconForStatus(status: string) {
	switch (status) {
		case DUUIStatus.Failed:
			return faX
		case DUUIStatus.Completed:
			return faCheck
		case DUUIStatus.Cancelled:
			return faCancel
		case DUUIStatus.Setup:
			return faWrench
		case DUUIStatus.Output:
			return faFileArrowUp
		case DUUIStatus.Running:
			return faRotate
		default:
			return faQuestion
	}
}

export function getProgressPercent(process: DUUIProcess, pipeline: DUUIPipeline) {
	const inputIsText: boolean = process.input.source === DUUIDocumentSource.Text.toLowerCase()
	const limit: number = inputIsText ? pipeline.components.length : process.documentCount
	if (!limit) {
		return 0
	} else {
		return Math.round((process.progress / limit) * 100)
	}
}

export function getProgressPercentLive(progress: number, limit: number) {
	if (!limit) {
		return 0
	} else {
		return Math.round((progress / limit) * 100)
	}
}

export function outputIsCloudProvider(outputType: DUUIDocumentOutput) {
	return (
		outputType === DUUIDocumentOutput.S3.toLowerCase() ||
		outputType === DUUIDocumentOutput.Dropbox.toLowerCase()
	)
}
