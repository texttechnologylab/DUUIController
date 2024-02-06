/**
 * Convert a duration in milliseconds to a formatted string with an appropriate unit.
 * Units may be ms, s, m and h representing different time intervals.
 *
 * @param durationInMilliseconds the duration in milliseconds
 * @returns a formatted string with an appropriate unit
 */
export const formatMilliseconds = (durationInMilliseconds: number) => {
	const hours = Math.floor(durationInMilliseconds / 3600000)
	const minutes = Math.floor((durationInMilliseconds % 3600000) / 60000)
	const seconds = Math.floor((durationInMilliseconds % 60000) / 1000)
	const milliseconds = durationInMilliseconds % 1000

	const hoursText = hours > 0 ? `${hours}h` : ''
	const minutesText = minutes > 0 ? `${minutes}m` : ''
	const secondsText = seconds > 0 && hours <= 0 ? `${seconds}s` : ''

	let result = [hoursText, minutesText, secondsText].filter(Boolean).join(' ')

	if (durationInMilliseconds < 1000) {
		result = `${result} ${milliseconds}ms`
	}

	return result || '0s'
}

/**
 * Get the difference of two POSIX timestamps formatted as a string.
 *
 * @param start The start time as a POSIX timestamp
 * @param end The end time as a POSIX timestamp
 * @returns The difference (end - start) formatted as a string.
 */
export const getDuration = (start: number | undefined, end: number | undefined) => {
	if (!start) return formatMilliseconds(0)

	if (!end) return formatMilliseconds(Math.ceil(Date.now() - start))

	return formatMilliseconds(Math.ceil(end - start))
}
