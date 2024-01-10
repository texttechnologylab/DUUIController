export const slugify = (text: string) =>
	text
		.toLowerCase()
		.trim()
		.replace(/[^\w\s-]/g, '')
		.replace(/[\s_-]+/g, '-')
		.replace(/^-+|-+$/g, '')

export const toTitleCase = (text: string) => {
	return text[0].toUpperCase() + text.slice(1)
}

export const dateToString = (date: Date) => {
	const options: Intl.DateTimeFormatOptions = {
		year: 'numeric',
		month: '2-digit',
		day: '2-digit'
	}
	return date.toLocaleString('de-DE', options)
}

export const datetimeToString = (date: Date) => {
	const options: Intl.DateTimeFormatOptions = {
		year: 'numeric',
		month: '2-digit',
		day: '2-digit',
		hour: '2-digit',
		minute: '2-digit',
		second: '2-digit'
	}
	return date.toLocaleString('de-DE', options)
}

export const equals = (first: string, second: string, ignoreCase: boolean = true) => {
	if (ignoreCase) {
		return first.toLowerCase() === second.toLowerCase()
	} else {
		return first === second
	}
}

export const includes = (first: string, second: string, ignoreCase: boolean = true) => {
	if (!first) return true

	if (ignoreCase) {
		return first.toLowerCase().includes(second.toLowerCase())
	} else {
		return first.includes(second)
	}
}

export const includesText = (values: string[], text: string, ignoreCase: boolean = true) => {
	if (!values) return false

	if (ignoreCase) {
		for (let value of values) {
			if (equals(value, text)) return true
		}
		return false
	} else {
		return values.includes(text)
	}
}

export const formatFileSize = (bytes: number, decimals: number = 0) => {
	if (!bytes) {
		return '0 B'
	}

	const factor = 1024
	const sizes = ['Bytes', 'KBytes', 'MBytes', 'GBytes', 'TBytes']
	const size = Math.floor(Math.log(bytes) / Math.log(factor))

	return `${parseFloat((bytes / Math.pow(factor, size)).toFixed(decimals))} ${sizes[size]}`
}

export function progresAsPercent(progress: number, maximum: number) {
	if (!maximum) return 0
	else {
		return Math.round((progress / maximum) * 100)
	}
}
