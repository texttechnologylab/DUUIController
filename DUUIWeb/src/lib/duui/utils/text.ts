/**
 * Convert any string to a slug meaning words are separated by hyphens.
 *
 * @param text The text to transform
 * @returns A string as a valid slug.
 */
export const slugify = (text: string) =>
	text
		.toLowerCase()
		.trim()
		.replace(/[^\w\s-]/g, '')
		.replace(/[\s_-]+/g, '-')
		.replace(/^-+|-+$/g, '')

/**
 * Convert any string to title case meaning words are not separated and the first letter is capitalized.
 *
 * @param text The text to transform
 * @returns A string in snake case.
 */
export const toTitleCase = (text: string) => {
	return text[0].toUpperCase() + text.slice(1)
}

/**
 * Convert any string to snake case meaning words are separated by underscores.
 *
 * @param text The text to transform
 * @returns A string in snake case.
 */
export const snakeToTitleCase = (text: string) =>
	text
		.replace(/^[-_]*(.)/, (_: any, c: string) => c.toUpperCase()) // Initial char (after -/_)
		.replace(/[-_]+(.)/g, (_: any, c: string) => ' ' + c.toUpperCase()) // First char after each -/_

/**
 * Convert a date to its string representation in the format dd.mm.yyyy (de-DE)
 * @param date The date to convert.
 * @returns a formatted date string.
 */
export const dateToString = (date: Date) => {
	const options: Intl.DateTimeFormatOptions = {
		year: 'numeric',
		month: '2-digit',
		day: '2-digit'
	}
	return date.toLocaleString('de-DE', options)
}

/**
 * Convert a date to its string representation in the format dd.mm.yyyy, hh:mm:ss,... (de-DE)
 * @param date The date to convert.
 * @returns a formatted date string.
 */
export const datetimeToString = (date: Date) => {
	const options: Intl.DateTimeFormatOptions = {
		year: 'numeric',
		month: '2-digit',
		day: '2-digit',
		hour: '2-digit',
		minute: '2-digit',
		second: '2-digit',
		fractionalSecondDigits: 3
	}
	return date.toLocaleString('de-DE', options)
}

/**
 * Check if first is equal to second. Can be made case insensitive.
 *
 * @param first The first text
 * @param second The second text
 * @param ignoreCase A flag that controls case sensitiviy
 * @returns if first is equal to second
 */
export const equals = (first: string, second: string, ignoreCase: boolean = true) => {
	if (ignoreCase) {
		return first.toLowerCase() === second.toLowerCase()
	} else {
		return first === second
	}
}

/**
 * Check if first includes second. Can be made case insensitive.
 *
 * @param first The text that may include in second
 * @param second The text that may be included in first
 * @param ignoreCase A flag that controls case sensitiviy
 * @returns if first contains second
 */
export const includes = (first: string, second: string, ignoreCase: boolean = true) => {
	if (!first) return true

	if (ignoreCase) {
		return first.toLowerCase().includes(second.toLowerCase())
	} else {
		return first.includes(second)
	}
}

/**
 * Check if a text is present in an array. Can be made case insensitive.
 *
 * @param values An array of string to check against
 * @param text The text to look for in the array
 * @param ignoreCase A flag that controls case sensitiviy
 * @returns if the array contains the text
 */
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

/**
 * Convert a number representing a size in bytes to a string with an appropriate suffix.
 *
 * @param bytes the file size in bytes
 * @param decimals number of decimal places
 * @returns a formatted string representing the size in bytes
 */
export const formatFileSize = (bytes: number, decimals: number = 0) => {
	if (!bytes) {
		return '0 B'
	}

	const factor = 1024
	const sizes = ['Bytes', 'KBytes', 'MBytes', 'GBytes', 'TBytes']
	const size = Math.floor(Math.log(bytes) / Math.log(factor))

	return `${parseFloat((bytes / Math.pow(factor, size)).toFixed(decimals))} ${sizes[size]}`
}

/**
 * Convert a progress number to a string.
 *
 * @param progress the current progress value
 * @param maximum the maximum progress possible
 * @returns the current progress as a percentage
 */
export function progresAsPercent(progress: number, maximum: number) {
	if (!maximum) return 0
	else {
		return Math.round((progress / maximum) * 100)
	}
}
